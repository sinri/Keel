package io.github.sinri.keel.web.http.receptionist;

import io.github.sinri.keel.helper.KeelHelpers;
import io.github.sinri.keel.logger.event.KeelEventLog;
import io.github.sinri.keel.logger.event.KeelEventLogger;
import io.github.sinri.keel.web.http.prehandler.KeelPlatformHandler;
import io.vertx.core.Handler;
import io.vertx.core.http.impl.CookieImpl;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.User;
import io.vertx.ext.web.RoutingContext;

import java.util.List;

/**
 * @since 2.9.2
 * @since 3.0.0 TEST PASSED
 */
public abstract class KeelWebReceptionist {
    private final RoutingContext routingContext;
    private final KeelEventLogger logger;

    public KeelWebReceptionist(RoutingContext routingContext) {
        this.routingContext = routingContext;
        this.logger = createLogger();
        Handler<KeelEventLog> previous = this.logger.getPresetEventLogEditor();
        this.logger.setPresetEventLogEditor(eventLog -> {
            eventLog
                    .put("request", new JsonObject()
                            .put("request_id", routingContext.get(KeelPlatformHandler.KEEL_REQUEST_ID))
                            .put("method", routingContext.request().method().name())
                            .put("path", routingContext.request().path())
                            .put("handler", this.getClass().getName())
                    );

            if (previous != null) {
                previous.handle(eventLog);
            }
        });
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
                    //.put("request_id", readRequestID())
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
                //.put("request_id", readRequestID())
                //.put("response", resp)
        );
        respondWithJsonObject(resp);
    }

    protected void respondOnFailure(Throwable throwable) {
        var resp = new JsonObject()
                .put("code", "FAILED")
                .put("data", throwable.getMessage());
        String error = KeelHelpers.stringHelper().renderThrowableChain(throwable);
        resp.put("throwable", error);
        logger.exception(throwable, event -> event
                        .message("RESPOND FAILURE")
                //.put("request_id", readRequestID())
        );
        respondWithJsonObject(resp);
    }

    public String readRequestID() {
        return routingContext.get(KeelPlatformHandler.KEEL_REQUEST_ID);
    }

    public long readRequestStartTime() {
        return routingContext.get(KeelPlatformHandler.KEEL_REQUEST_START_TIME);
    }

    public List<String> readRequestIPChain() {
        return KeelHelpers.netHelper().parseWebClientIPChain(routingContext);
//        return routingContext.get(KeelPlatformHandler.KEEL_REQUEST_CLIENT_IP_CHAIN);
    }

    public User readRequestUser() {
        return routingContext.user();
    }

    /**
     * @since 3.0.1
     */
    protected void addCookie(String name, String value, Long maxAge, boolean httpOnly) {
        CookieImpl cookie = new CookieImpl(name, value);
        cookie.setPath("/");
        cookie.setMaxAge(maxAge);
        cookie.setHttpOnly(httpOnly);
        getRoutingContext().response().addCookie(cookie);
    }

    /**
     * @since 3.0.1
     */
    protected void addCookie(String name, String value, Long maxAge) {
        addCookie(name, value, maxAge, false);
    }

    /**
     * @since 3.0.1
     */
    protected void removeCookie(String name) {
        getRoutingContext().response().removeCookie(name);
    }
}
