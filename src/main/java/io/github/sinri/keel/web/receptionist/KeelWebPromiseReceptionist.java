package io.github.sinri.keel.web.receptionist;

import io.github.sinri.keel.core.controlflow.PromiseTimeout;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.ext.web.RoutingContext;

/**
 * @since 2.9.2
 */
public abstract class KeelWebPromiseReceptionist extends KeelWebFutureReceptionist {

    private long maxPromiseWaitTime = 10_000L;

    public KeelWebPromiseReceptionist(RoutingContext routingContext) {
        super(routingContext);
    }

    protected KeelWebPromiseReceptionist setMaxPromiseWaitTime(long maxPromiseWaitTime) {
        this.maxPromiseWaitTime = maxPromiseWaitTime;
        return this;
    }

    @Override
    public Future<Object> handleForFuture() {
        Promise<Object> promise = Promise.promise();

        getRoutingContext().vertx()
                .setTimer(maxPromiseWaitTime, timerID -> promise
                        .tryFail(new PromiseTimeout(maxPromiseWaitTime)));

        handleForPromise(promise);

        return promise.future();
    }

    abstract protected void handleForPromise(Promise<Object> promise);

    @Override
    protected void respondOnFailure(Throwable throwable) {
        if (throwable instanceof PromiseTimeout) {
            getRoutingContext().fail(504, throwable);
        } else {
            super.respondOnFailure(throwable);
        }
    }
}
