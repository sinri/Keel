package io.github.sinri.keel.web.http.handler;

import io.github.sinri.keel.logger.event.KeelEventLog;
import io.github.sinri.keel.logger.event.KeelEventLogger;
import io.github.sinri.keel.web.http.prehandler.KeelPlatformHandler;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

import static io.github.sinri.keel.helper.KeelHelpersInterface.KeelHelpers;

/**
 * 这个类是个针对某一类RoutingContext的handler，而不是针对某一个RoutingContext的handler！！！
 *
 * @since 2.9
 * @since 2.9.2 Remove Property `RoutingContext`.
 * @since 3.0.0 TEST PASSED
 */
@Deprecated(since = "3.0.12")
abstract public class KeelWebRequestHandler implements Handler<RoutingContext> {
    protected static final String KEEL_REQUEST_LOGGER = "KEEL_REQUEST_LOGGER";

    public KeelWebRequestHandler() {
    }

    abstract protected KeelEventLogger createLogger(RoutingContext routingContext);

    protected void respondOnSuccess(RoutingContext routingContext, Object data) {
        var logger = getLogger(routingContext);

        JsonObject resp = new JsonObject()
                .put("code", "OK")
                .put("data", data);
        logger.info(eventLog -> eventLog
                .message("RESPOND SUCCESS")
                .put("response", resp));

        try {
            routingContext.json(resp);
        } catch (Throwable throwable) {
            logger.exception(throwable, eventLog -> eventLog
                    .message("RoutingContext has been dealt by others")
                    .put("response", new JsonObject()
                            .put("code", routingContext.response().getStatusCode())
                            .put("message", routingContext.response().getStatusMessage())
                            .put("ended", routingContext.response().ended())
                            .put("closed", routingContext.response().closed()))
            );
        }
    }

    protected void respondOnFailure(RoutingContext routingContext, Throwable throwable) {
        var logger = getLogger(routingContext);

        var x = new JsonObject()
                .put("code", "FAILED")
                .put("data", throwable.getMessage());
        String error = KeelHelpers.stringHelper().renderThrowableChain(throwable);
        x.put("throwable", error);
        logger.error(eventLog -> eventLog
                .message("RESPOND FAILURE")
                .put("response", x)
        );
        try {
            routingContext.json(x);
        } catch (Throwable throwable2) {
            logger.exception(throwable2, eventLog -> eventLog
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

    public final void handle(RoutingContext routingContext) {
        KeelEventLogger logger = createLogger(routingContext);
        logger.setPresetEventLogEditor(eventLog -> {
            eventLog
                    .put("request", new JsonObject()
                            .put("request_id", routingContext.get(KeelPlatformHandler.KEEL_REQUEST_ID))
                            .put("method", routingContext.request().method().name())
                            .put("path", routingContext.request().path())
                            .put("handler", this.getClass().getName())
                    );

            Handler<KeelEventLog> presetEventLogEditor = logger.getPresetEventLogEditor();
            if (presetEventLogEditor != null) {
                presetEventLogEditor.handle(eventLog);
            }
        });
        routingContext.put(KEEL_REQUEST_LOGGER, logger);

        handleRequest(routingContext);
    }

    /**
     * @since 3.0.0
     */
    protected final KeelEventLogger getLogger(RoutingContext routingContext) {
        return routingContext.get(KEEL_REQUEST_LOGGER);
    }

    abstract protected void handleRequest(RoutingContext routingContext);
}
