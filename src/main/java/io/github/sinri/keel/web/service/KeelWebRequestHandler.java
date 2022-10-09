package io.github.sinri.keel.web.service;

import io.github.sinri.keel.Keel;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

/**
 * @since 2.8.1
 */
public abstract class KeelWebRequestHandler implements Handler<RoutingContext> {
    private RoutingContext routingContext;
    private boolean verbose = false;

    public RoutingContext getRoutingContext() {
        return routingContext;
    }

    public KeelWebRequestHandler setVerbose(boolean verbose) {
        this.verbose = verbose;
        return this;
    }

    @Override
    public final void handle(RoutingContext routingContext) {
        this.routingContext = routingContext;
        Future.succeededFuture()
                .compose(v -> handleRequest(routingContext))
                .onSuccess(this::respondOnSuccess)
                .onFailure(this::respondOnFailure);
    }

    abstract protected Future<Object> handleRequest(RoutingContext routingContext);

    protected void respondOnSuccess(Object data) {
        routingContext.json(new JsonObject()
                .put("code", "OK")
                .put("data", data)
        );
    }

    protected void respondOnFailure(Throwable throwable) {
        var x = new JsonObject()
                .put("code", "FAILED")
                .put("data", throwable.getMessage());
        if (verbose) {
            String error = Keel.stringHelper().renderThrowableChain(throwable);
            x.put("throwable", error);
        }
        routingContext.json(x);
    }
}
