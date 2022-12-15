package io.github.sinri.keel.web.service;

import io.github.sinri.keel.lagecy.Keel;
import io.github.sinri.keel.lagecy.core.logger.KeelLogger;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

/**
 * 这个类是个针对某一类RoutingContext的handler，而不是针对某一个RoutingContext的handler！！！
 *
 * @since 2.9
 * @since 2.9.2 Remove Property `RoutingContext`.
 */
abstract public class KeelWebRequestHandler implements Handler<RoutingContext> {
    private boolean verbose = false;

    /**
     * @since 2.9.1
     */
    public boolean isVerbose() {
        return verbose;
    }

    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    /**
     * @since 2.9.2
     */
    public KeelLogger createLogger() {
        return createLogger(null);
    }

    /**
     * @since 2.9.2
     */
    abstract public KeelLogger createLogger(RoutingContext routingContext);

    protected void respondOnSuccess(RoutingContext routingContext, Object data) {
        KeelLogger logger = createLogger(routingContext);
        JsonObject resp = new JsonObject()
                .put("code", "OK")
                .put("data", data);
        if (this.isVerbose()) {
            logger.info("RESPOND SUCCESS", resp);
        }
        try {
            routingContext.json(resp);
        } catch (Throwable throwable) {
            logger.exception(throwable);
            logger.error("RoutingContext has been dealt by others", new JsonObject()
                    .put("response", new JsonObject()
                            .put("code", routingContext.response().getStatusCode())
                            .put("message", routingContext.response().getStatusMessage())
                            .put("ended", routingContext.response().ended())
                            .put("closed", routingContext.response().closed())
                    )
            );
        }
    }

    protected void respondOnFailure(RoutingContext routingContext, Throwable throwable) {
        KeelLogger logger = createLogger(routingContext);
        var x = new JsonObject()
                .put("code", "FAILED")
                .put("data", throwable.getMessage());
        if (isVerbose()) {
            String error = Keel.helpers().string().renderThrowableChain(throwable);
            x.put("throwable", error);
            logger.exception("RESPOND FAILURE", throwable);
        } else {
            logger.error("RESPOND FAILURE: " + throwable);
        }
        try {
            routingContext.json(x);
        } catch (Throwable throwable2) {
            logger.exception(throwable2);
            logger.error("RoutingContext has been dealt by others", new JsonObject()
                    .put("response", new JsonObject()
                            .put("code", routingContext.response().getStatusCode())
                            .put("message", routingContext.response().getStatusMessage())
                            .put("ended", routingContext.response().ended())
                            .put("closed", routingContext.response().closed())
                    )
            );
        }
    }

    public final void handle(RoutingContext routingContext) {
        handleRequest(routingContext);
    }

    abstract protected void handleRequest(RoutingContext routingContext);
}
