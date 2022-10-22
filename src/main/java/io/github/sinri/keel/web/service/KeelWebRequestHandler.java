package io.github.sinri.keel.web.service;

import io.github.sinri.keel.Keel;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

/**
 * @since 2.9
 */
abstract public class KeelWebRequestHandler implements Handler<RoutingContext> {
    private RoutingContext routingContext;
    private boolean verbose = false;

    public RoutingContext getRoutingContext() {
        return routingContext;
    }

    public void setRoutingContext(RoutingContext routingContext) {
        this.routingContext = routingContext;
    }

    public boolean getVerbose() {
        return verbose;
    }

    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    protected void respondOnSuccess(Object data) {
        getRoutingContext().json(new JsonObject()
                .put("code", "OK")
                .put("data", data)
        );
    }

    protected void respondOnFailure(Throwable throwable) {
        var x = new JsonObject()
                .put("code", "FAILED")
                .put("data", throwable.getMessage());
        if (getVerbose()) {
            String error = Keel.stringHelper().renderThrowableChain(throwable);
            x.put("throwable", error);
        }
        getRoutingContext().json(x);
    }

    public final void handle(RoutingContext routingContext) {
        setRoutingContext(routingContext);
        handleRequest();
    }

    abstract protected void handleRequest();
}
