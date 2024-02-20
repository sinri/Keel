package io.github.sinri.keel.web.http.receptionist;

import io.github.sinri.keel.logger.event.KeelEventLogger;
import io.github.sinri.keel.web.http.prehandler.KeelPlatformHandler;
import io.vertx.core.http.impl.CookieImpl;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.User;
import io.vertx.ext.web.RoutingContext;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Supplier;

import static io.github.sinri.keel.helper.KeelHelpersInterface.KeelHelpers;

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
        // since 3.1.10
        this.logger.setBaseLogBuilder((Supplier<KeelWebReceptionistRequestEventLog>) () -> new KeelWebReceptionistRequestEventLog(logger.getPresetTopic(), routingContext));
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
                    .context(c -> c
                            .put("response", new JsonObject()
                                    .put("code", routingContext.response().getStatusCode())
                                    .put("message", routingContext.response().getStatusMessage())
                                    .put("ended", routingContext.response().ended())
                                    .put("closed", routingContext.response().closed())
                            )
                    )
            );
        }
    }

    /**
     * @since 3.0.12 add request_id to output json object
     */
    protected void respondOnSuccess(Object data) {
        JsonObject resp = new JsonObject()
                .put("request_id", routingContext.get(KeelPlatformHandler.KEEL_REQUEST_ID))
                .put("code", "OK")
                .put("data", data);
        logger.info(event -> event.message("RESPOND SUCCESS"));
        respondWithJsonObject(resp);
    }

    /**
     * @since 3.0.12 add request_id to output json object
     */
    protected void respondOnFailure(Throwable throwable) {
        var resp = new JsonObject()
                .put("request_id", routingContext.get(KeelPlatformHandler.KEEL_REQUEST_ID))
                .put("code", "FAILED")
                .put("data", throwable.getMessage());
        String error = KeelHelpers.stringHelper().renderThrowableChain(throwable);
        resp.put("throwable", error);
        logger.exception(throwable, event -> event.message("RESPOND FAILURE"));
        respondWithJsonObject(resp);
    }

    /**
     * @since 3.0.8 mark it nullable as it might be null.
     */
    public @Nullable String readRequestID() {
        return routingContext.get(KeelPlatformHandler.KEEL_REQUEST_ID);
    }

    /**
     * @since 3.0.8 mark it nullable as it might be null.
     */
    public @Nullable Long readRequestStartTime() {
        return routingContext.get(KeelPlatformHandler.KEEL_REQUEST_START_TIME);
    }

    public List<String> readRequestIPChain() {
        return KeelHelpers.netHelper().parseWebClientIPChain(routingContext);
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
