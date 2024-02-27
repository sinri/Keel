package io.github.sinri.keel.web.http.receptionist;

import io.github.sinri.keel.logger.issue.center.KeelIssueRecordCenter;
import io.github.sinri.keel.logger.issue.recorder.KeelIssueRecorder;
import io.github.sinri.keel.web.http.prehandler.KeelPlatformHandler;
import io.vertx.core.http.impl.CookieImpl;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.User;
import io.vertx.ext.web.RoutingContext;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;

import static io.github.sinri.keel.helper.KeelHelpersInterface.KeelHelpers;

/**
 * @since 2.9.2
 * @since 3.0.0 TEST PASSED
 */
public abstract class KeelWebReceptionist {
    private final @Nonnull RoutingContext routingContext;
    private final @Nonnull KeelIssueRecorder<ReceptionistIssueRecord> issueRecorder;

    public KeelWebReceptionist(@Nonnull RoutingContext routingContext) {
        this.routingContext = routingContext;
        this.issueRecorder = issueRecordCenter().generateIssueRecorder(ReceptionistIssueRecord.TopicReceptionist, () -> new ReceptionistIssueRecord(readRequestID()));
        this.issueRecorder.info(r -> r.setRequest(
                routingContext.request().method(),
                routingContext.request().path(),
                this.getClass()
        ));
    }

    @Nonnull
    protected final RoutingContext getRoutingContext() {
        return routingContext;
    }

    /**
     * @since 3.2.0
     */
    @Nonnull
    abstract protected KeelIssueRecordCenter issueRecordCenter();

    /**
     * @since 3.2.0
     */
    @Nonnull
    public final KeelIssueRecorder<ReceptionistIssueRecord> getIssueRecorder() {
        return issueRecorder;
    }

    abstract public void handle();

    private void respondWithJsonObject(@Nonnull JsonObject resp) {
        try {
            routingContext.json(resp);
        } catch (Throwable throwable) {
            getIssueRecorder().exception(throwable, event -> event
                    .message("RoutingContext has been dealt by others")
                    .setResponse(
                            routingContext.response().getStatusCode(),
                            routingContext.response().getStatusMessage(),
                            routingContext.response().ended(),
                            routingContext.response().closed()
                    )
            );
        }
    }

    /**
     * @since 3.0.12 add request_id to output json object
     */
    protected void respondOnSuccess(@Nullable Object data) {
        JsonObject resp = new JsonObject()
                .put("request_id", routingContext.get(KeelPlatformHandler.KEEL_REQUEST_ID))
                .put("code", "OK")
                .put("data", data);
        getIssueRecorder().info(r -> r.message("SUCCESS, TO RESPOND."));
        respondWithJsonObject(resp);
    }

    /**
     * @since 3.0.12 add request_id to output json object
     */
    protected void respondOnFailure(@Nonnull Throwable throwable) {
        var resp = new JsonObject()
                .put("request_id", routingContext.get(KeelPlatformHandler.KEEL_REQUEST_ID))
                .put("code", "FAILED")
                .put("data", throwable.getMessage());
        String error = KeelHelpers.stringHelper().renderThrowableChain(throwable);
        resp.put("throwable", error);
        getIssueRecorder().exception(throwable, r -> r.message("FAILED, TO RESPOND."));
        respondWithJsonObject(resp);
    }

    /**
     * @since 3.0.8 mark it nullable as it might be null.
     */
    public @Nonnull String readRequestID() {
        return Objects.requireNonNull(routingContext.get(KeelPlatformHandler.KEEL_REQUEST_ID));
    }

    /**
     * @since 3.0.8 mark it nullable as it might be null.
     */
    public @Nonnull Long readRequestStartTime() {
        return Objects.requireNonNull(routingContext.get(KeelPlatformHandler.KEEL_REQUEST_START_TIME));
    }

    public @Nonnull List<String> readRequestIPChain() {
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
    protected void addCookie(@Nonnull String name, @Nonnull String value, Long maxAge) {
        addCookie(name, value, maxAge, false);
    }

    /**
     * @since 3.0.1
     */
    protected void removeCookie(@Nonnull String name) {
        getRoutingContext().response().removeCookie(name);
    }

}
