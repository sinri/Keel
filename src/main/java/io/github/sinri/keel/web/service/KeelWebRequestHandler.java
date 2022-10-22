package io.github.sinri.keel.web.service;

import io.github.sinri.keel.Keel;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

/**
 * @since 2.8.1
 */
public interface KeelWebRequestHandler extends Handler<RoutingContext> {


    RoutingContext getRoutingContext();

    void setRoutingContext(RoutingContext routingContext);

    boolean getVerbose();

    void setVerbose(boolean verbose);

    /**
     * If using Future would not be easy, override it.
     */
    void handleRequest();

    default void respondOnSuccess(Object data) {
        getRoutingContext().json(new JsonObject()
                .put("code", "OK")
                .put("data", data)
        );
    }

    default void respondOnFailure(Throwable throwable) {
        var x = new JsonObject()
                .put("code", "FAILED")
                .put("data", throwable.getMessage());
        if (getVerbose()) {
            String error = Keel.stringHelper().renderThrowableChain(throwable);
            x.put("throwable", error);
        }
        getRoutingContext().json(x);
    }
}
