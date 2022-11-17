package io.github.sinri.keel.web.receptionist;

import io.github.sinri.keel.Keel;
import io.github.sinri.keel.core.logger.KeelLogger;
import io.github.sinri.keel.web.handler.KeelPlatformHandler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.User;
import io.vertx.ext.web.RoutingContext;

import java.util.List;

/**
 * @since 2.9.2
 */
public abstract class KeelWebReceptionist {
    private final RoutingContext routingContext;
    private final KeelLogger logger;

    public KeelWebReceptionist(RoutingContext routingContext) {
        this.routingContext = routingContext;
        this.logger = createLogger();
    }

    protected RoutingContext getRoutingContext() {
        return routingContext;
    }

    abstract protected KeelLogger createLogger();

    abstract public void handle();

    private void respondWithJsonObject(JsonObject resp) {
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

    protected void respondOnSuccess(Object data) {
        JsonObject resp = new JsonObject()
                .put("code", "OK")
                .put("data", data);
        logger.info("RESPOND SUCCESS", resp);
        respondWithJsonObject(resp);
    }

    protected void respondOnFailure(Throwable throwable) {
        var resp = new JsonObject()
                .put("code", "FAILED")
                .put("data", throwable.getMessage());
        String error = Keel.helpers().string().renderThrowableChain(throwable);
        resp.put("throwable", error);
        logger.exception("RESPOND FAILURE", throwable);
        respondWithJsonObject(resp);
    }

    public String readRequestID() {
        return routingContext.get(KeelPlatformHandler.KEEL_REQUEST_ID);
    }

    public long readRequestStartTime() {
        return routingContext.get(KeelPlatformHandler.KEEL_REQUEST_START_TIME);
    }

    public List<String> readRequestIPChain() {
        return routingContext.get(KeelPlatformHandler.KEEL_REQUEST_CLIENT_IP_CHAIN);
    }

    public User readRequestUser() {
        return routingContext.user();
    }
}
