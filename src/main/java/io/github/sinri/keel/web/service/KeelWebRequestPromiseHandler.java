package io.github.sinri.keel.web.service;

import io.github.sinri.keel.facade.async.PromiseTimeout;
import io.github.sinri.keel.lagecy.Keel;
import io.vertx.core.Promise;
import io.vertx.ext.web.RoutingContext;

/**
 * @since 2.9
 */
public abstract class KeelWebRequestPromiseHandler extends KeelWebRequestHandler {
    private Long timerID;

    protected long timeout() {
        return 30_000L;
    }

    /**
     * @param promise build response for this request
     */
    abstract protected void handleRequestForFuture(RoutingContext routingContext, Promise<Object> promise);

    @Override
    public final void handleRequest(RoutingContext routingContext) {
        Promise<Object> respPromise = Promise.promise();
        timerID = Keel.getVertx().setTimer(timeout(), v -> {
            timerID = null;
            respPromise.tryFail(new PromiseTimeout(timeout()));
        });
        handleRequestForFuture(routingContext, respPromise);
        respPromise.future()
                .andThen(ar -> {
                    if (ar.failed()) {
                        this.respondOnFailure(routingContext, ar.cause());
                    } else {
                        this.respondOnSuccess(routingContext, ar.result());
                    }
                });
    }

    @Override
    protected void respondOnFailure(RoutingContext routingContext, Throwable throwable) {
        if (throwable instanceof PromiseTimeout) {
            routingContext.fail(504, throwable);
        } else {
            super.respondOnFailure(routingContext, throwable);
        }
    }
}
