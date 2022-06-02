package io.github.sinri.keel.core.semaphore;

import io.github.sinri.keel.Keel;
import io.github.sinri.keel.core.logger2.KeelLogger;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.core.shareddata.Counter;

import java.util.function.Function;

/**
 * @since 1.3
 */
public class KeelShareDataSemaphore {
    final private String name;
    final private int permits;

    private final KeelLogger logger;

    public KeelShareDataSemaphore(String name, int permits) {
        this.name = name;
        this.permits = permits;
        this.logger = KeelLogger.silentLogger();
    }

    public KeelShareDataSemaphore(String name, int permits, KeelLogger logger) {
        this.name = name;
        this.permits = permits;
        this.logger = logger;
    }

    protected Future<Counter> getCounter() {
        return Keel.getVertx().sharedData().getCounter(name);
    }

    public Future<Boolean> isNowAvailable() {
        return getCounter().compose(Counter::get).compose(current -> Future.succeededFuture(current < permits));
    }

    public Future<Long> getAvailablePermits() {
        return getCounter().compose(Counter::get).compose(current -> Future.succeededFuture(permits - current));
    }

    /**
     * @param function the function to execute
     * @return released Future, succeed or failed
     */
    public Future<Void> tryExecute(Function<Void, Future<Void>> function) {
        return acquire().compose(acquired -> {
            if (acquired) {
                // always return succeed future
                return function.apply(null)
                        .recover(throwable -> {
                            logger.warning(getClass() + " tryExecute failed in function apply: " + throwable.getMessage());
                            return Future.succeededFuture();
                        })
                        .compose(v -> release());
            } else {
                // always return failed future
                return release().compose(v -> Future.failedFuture(getClass() + " tryExecute failed: not acquired"));
            }
        });
    }

    protected Future<Boolean> acquire() {
        return getCounter()
                .compose(Counter::incrementAndGet)
                .compose(current -> {
                    logger.debug(getClass() + " acquire, current " + current, new JsonObject().put("runnable", current <= permits));
                    if (current <= permits) {
                        return Future.succeededFuture(true);
                    } else {
                        return Future.succeededFuture(false);
                    }
                });
    }

    protected Future<Void> release() {
        return getCounter()
                .compose(Counter::decrementAndGet)
                .compose(current -> {
                    logger.debug(getClass() + " release, current " + current);
                    return Future.succeededFuture();
                });
    }
}
