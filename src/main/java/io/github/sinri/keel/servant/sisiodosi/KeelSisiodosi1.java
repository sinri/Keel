package io.github.sinri.keel.servant.sisiodosi;

import io.github.sinri.keel.Keel;
import io.github.sinri.keel.core.controlflow.FutureUntil;
import io.github.sinri.keel.core.logger.KeelLogLevel;
import io.github.sinri.keel.core.logger.KeelLogger;
import io.vertx.core.Future;
import io.vertx.core.eventbus.MessageConsumer;

import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * <p>
 * ししおどし（鹿威し）とは、田畑を荒らす鳥獣を威嚇し追い払うために設けられる装置類の総称。
 * かかし・鳴子・添水（そうず）。
 * </p>
 * <p>
 * Like KeelEndless, it use EventBus to notify rather than timer.
 * </p>
 * <p>
 * (1) start
 * (2)
 * </p>
 *
 * @since 2.8
 */
@Deprecated(since = "2.8.1")
public class KeelSisiodosi1 {
    private final String eventBusAddress;
    private final Queue<Function<Void, Future<Void>>> dripQueue;
    private KeelLogger logger;
    private MessageConsumer<Object> consumer;
    private Supplier<Long> lockAcquireTimoutSupplier = () -> 10L;
    private Supplier<Long> lockAcquireRetryDelaySupplier = () -> {
        return (long) (Math.random() * 30L + 10L);
    };

    public KeelSisiodosi1(String eventBusAddress) {
        this.eventBusAddress = eventBusAddress;
        this.dripQueue = new ConcurrentLinkedQueue<>();
        this.logger = KeelLogger.silentLogger();

        start();
    }

    public KeelLogger getLogger() {
        return logger;
    }

    public KeelSisiodosi1 setLogger(KeelLogger logger) {
        this.logger = logger;
        return this;
    }

    private void sendDropHandlerWorkMessage() {
        Keel.getEventBus().send(eventBusAddress, null);
    }

    private Future<Void> cleanDropHandlerQueue() {
        this.logger.debug("cleanDropHandlerQueue start");
        return FutureUntil.call(
                new Supplier<Future<Boolean>>() {
                    @Override
                    public Future<Boolean> get() {
                        Function<Void, Future<Void>> drop = dripQueue.poll();
                        if (drop == null) {
                            return Future.succeededFuture(true);
                        }
                        return Future.succeededFuture()
                                .compose(v -> drop.apply(null))
                                .compose(
                                        done -> Future.succeededFuture(false),
                                        throwable -> {
                                            logger.exception("DROP ERROR", throwable);
                                            return Future.succeededFuture(false);
                                        }
                                );
                    }
                }
        );
    }

    /**
     * @since 2.8.1
     */
    public int getDripQueueSize() {
        return dripQueue.size();
    }

    public void drop(Function<Void, Future<Void>> drip) {
        dripQueue.offer(drip);
        sendDropHandlerWorkMessage();
    }

    public KeelSisiodosi1 setLockAcquireRetryDelaySupplier(Supplier<Long> lockAcquireRetryDelaySupplier) {
        Objects.requireNonNull(lockAcquireRetryDelaySupplier);
        this.lockAcquireRetryDelaySupplier = lockAcquireRetryDelaySupplier;
        return this;
    }

    public KeelSisiodosi1 setLockAcquireTimoutSupplier(Supplier<Long> lockAcquireTimoutSupplier) {
        Objects.requireNonNull(lockAcquireTimoutSupplier);
        this.lockAcquireTimoutSupplier = lockAcquireTimoutSupplier;
        return this;
    }

    protected void start() {
        if (this.consumer != null) {
            throw new RuntimeException("START WITH NON-NULL CONSUMER???");
        }

        this.consumer = Keel.getVertx().eventBus().consumer(eventBusAddress);
        this.consumer
                .handler(message -> {
                    // drip comes
                    Keel.getVertx().sharedData().getLockWithTimeout(eventBusAddress, lockAcquireTimoutSupplier.get(), asyncLock -> {
                        if (asyncLock.failed()) {
                            getLogger().exception(KeelLogLevel.DEBUG, "ACQUIRE LOCK FAILED", asyncLock.cause());
                            long delay = lockAcquireRetryDelaySupplier.get();
                            Keel.getVertx().setTimer(delay, timer -> {
                                sendDropHandlerWorkMessage();
                            });
                            return;
                        }

                        getLogger().debug("ACQUIRED LOCK");
                        cleanDropHandlerQueue()
                                .onComplete(done -> {
                                    asyncLock.result().release();
                                    getLogger().debug("RELEASED LOCK");
                                });
                    });
                })
                .endHandler(end -> {
                    // Set an end handler.
                    // Once the stream has ended, and there is no more data to be read,
                    // this handler will be called.
                    getLogger().debug("END");
                })
                .exceptionHandler(throwable -> {
                    // Set an exception handler on the read stream.
                    getLogger().exception("ERROR", throwable);
                })
                .completionHandler(ar -> {
                    // Optional method which can be called to indicate
                    // when the registration has been propagated across the cluster.
                });
    }

    public Future<Void> stop() {
        if (this.consumer != null) {
            return this.consumer.unregister()
                    .onSuccess(v -> this.consumer = null);
        } else {
            return Future.succeededFuture();
        }
    }

    public Future<Void> restart() {
        return stop()
                .compose(v -> {
                    start();
                    return Future.succeededFuture();
                });
    }
}
