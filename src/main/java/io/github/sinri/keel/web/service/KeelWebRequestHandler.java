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
        try {
            getRoutingContext().json(resp);
        } catch (Throwable throwable) {
            logger().exception(throwable);
            logger().error("RoutingContext has been dealt by others", new JsonObject()
                    .put("response", new JsonObject()
                            .put("code", getRoutingContext().response().getStatusCode())
                            .put("message", getRoutingContext().response().getStatusMessage())
                            .put("ended", getRoutingContext().response().ended())
                            .put("closed", getRoutingContext().response().closed())
                    )
            );
        }
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
        try {
            getRoutingContext().json(x);
        } catch (Throwable throwable2) {
            logger().exception(throwable2);
            logger().error("RoutingContext has been dealt by others", new JsonObject()
                    .put("response", new JsonObject()
                            .put("code", getRoutingContext().response().getStatusCode())
                            .put("message", getRoutingContext().response().getStatusMessage())
                            .put("ended", getRoutingContext().response().ended())
                            .put("closed", getRoutingContext().response().closed())
                    )
            );
        }
    }

    public final void handle(RoutingContext routingContext) {
        this.routingContext = routingContext;
        handleRequest();
    }

    abstract protected void handleRequest();
}
