package io.github.sinri.keel.web.http.receptionist;

import io.github.sinri.keel.facade.Keel;
import io.github.sinri.keel.logger.event.KeelEventLogger;
import io.github.sinri.keel.web.http.prehandler.KeelPlatformHandler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.User;
import io.vertx.ext.web.RoutingContext;

import java.util.List;

/**
 * @since 2.9.2
 * @since 3.0.0 TEST PASSED
 */
public abstract class KeelWebReceptionist {
    private final Keel keel;
    private final RoutingContext routingContext;
    private final KeelEventLogger logger;

    public KeelWebReceptionist(Keel keel, RoutingContext routingContext) {
        this.keel = keel;
        this.routingContext = routingContext;
        this.logger = createLogger();
    }

    public Keel getKeel() {
        return keel;
    }

    protected RoutingContext getRoutingContext() {
        return routingContext;
    }

    abstract protected KeelEventLogger createLogger();

    public KeelEventLogger getLogger() {
        return logger;
    }

    abstract public void handle();

    private void respondWithJsonObject(JsonObject resp) {
        try {
            routingContext.json(resp);
        } catch (Throwable throwable) {
            logger.exception(throwable, event -> event
                    .message("RoutingContext has been dealt by others")
                    .put("response", new JsonObject()
                            .put("code", routingContext.response().getStatusCode())
                            .put("message", routingContext.response().getStatusMessage())
                            .put("ended", routingContext.response().ended())
                            .put("closed", routingContext.response().closed())
                    )
            );
        }
    }

    protected void respondOnSuccess(Object data) {
        JsonObject resp = new JsonObject()
                .put("code", "OK")
                .put("data", data);
        logger.info(event -> event
                .message("RESPOND SUCCESS")
                .put("response", resp));
        respondWithJsonObject(resp);
    }

    protected void respondOnFailure(Throwable throwable) {
        var resp = new JsonObject()
                .put("code", "FAILED")
                .put("data", throwable.getMessage());
        String error = keel.stringHelper().renderThrowableChain(throwable);
        resp.put("throwable", error);
        logger.exception(throwable, "RESPOND FAILURE");
        respondWithJsonObject(resp);
    }

    public String readRequestID() {
        return routingContext.get(KeelPlatformHandler.KEEL_REQUEST_ID);
    }

    public long readRequestStartTime() {
        return routingContext.get(KeelPlatformHandler.KEEL_REQUEST_START_TIME);
    }

    public List<String> readRequestIPChain() {
        return keel.netHelper().parseWebClientIPChain(routingContext);
//        return routingContext.get(KeelPlatformHandler.KEEL_REQUEST_CLIENT_IP_CHAIN);
    }

    public User readRequestUser() {
        return routingContext.user();
    }
}
