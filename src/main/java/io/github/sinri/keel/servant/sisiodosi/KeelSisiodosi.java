package io.github.sinri.keel.servant.sisiodosi;

import io.github.sinri.keel.Keel;
import io.github.sinri.keel.core.controlflow.FutureUntil;
import io.github.sinri.keel.core.logger.KeelLogger;
import io.vertx.core.Future;

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
 *
 * @since 2.8
 */
public class KeelSisiodosi {
    private final String eventBusAddress;
    private final Queue<Function<Void, Future<Void>>> dripQueue;
    private KeelLogger logger;

    public KeelSisiodosi(String eventBusAddress) {
        this.eventBusAddress = eventBusAddress;
        this.dripQueue = new ConcurrentLinkedQueue<>();
        this.logger = KeelLogger.silentLogger();//Keel.outputLogger("KeelSisiodosi").setCategoryPrefix(eventBusAddress);

        Keel.getEventBus().consumer(eventBusAddress, message -> {
            // drip comes
            Keel.getVertx().sharedData().getLock(eventBusAddress)
                    .onComplete(asyncLock -> {
                        if (asyncLock.failed()) {
                            getLogger().exception("ACQUIRE LOCK FAILED", asyncLock.cause());
                            Keel.getVertx().setTimer(1000L, timer -> {
                                getLogger().debug("sendDropHandlerWorkMessage, NOW 1s LATER SINCE ACQUIRE LOCK FAILED");
                                sendDropHandlerWorkMessage();
                            });
                        } else {
                            getLogger().debug("ACQUIRED LOCK");
                            cleanDropHandlerQueue()
                                    .onComplete(done -> {
                                        asyncLock.result().release();
                                        getLogger().debug("RELEASED LOCK");
                                    });
                        }
                    });
        });
    }

    public KeelLogger getLogger() {
        return logger;
    }

    public KeelSisiodosi setLogger(KeelLogger logger) {
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

    public void drop(Function<Void, Future<Void>> drip) {
        dripQueue.offer(drip);
        sendDropHandlerWorkMessage();
    }
}
