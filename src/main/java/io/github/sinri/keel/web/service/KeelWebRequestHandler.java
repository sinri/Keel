package io.github.sinri.keel.web.service;

import io.github.sinri.keel.Keel;
import io.github.sinri.keel.core.logger.KeelLogger;
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

    /**
     * @since 2.9.1
     */
    public boolean isVerbose() {
        return verbose;
    }

    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    abstract public KeelLogger logger();

    protected void respondOnSuccess(Object data) {
        JsonObject resp = new JsonObject()
                .put("code", "OK")
                .put("data", data);
        if (this.isVerbose()) {
            logger().info("RESPOND SUCCESS", resp);
        }
        getRoutingContext().json(resp);
    }

    protected void respondOnFailure(Throwable throwable) {
        var x = new JsonObject()
                .put("code", "FAILED")
                .put("data", throwable.getMessage());
        if (isVerbose()) {
            String error = Keel.helpers().string().renderThrowableChain(throwable);
            x.put("throwable", error);
            logger().exception("RESPOND FAILURE", throwable);
        } else {
            logger().error("RESPOND FAILURE: " + throwable);
        }

        getRoutingContext().json(x);
    }

    public final void handle(RoutingContext routingContext) {
        setRoutingContext(routingContext);
        handleRequest();
    }

    abstract protected void handleRequest();
}
