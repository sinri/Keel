package io.github.sinri.keel.web.http.receptionist;

import io.github.sinri.keel.logger.KeelLogLevel;
import io.github.sinri.keel.logger.event.KeelEventLogImpl;
import io.github.sinri.keel.web.http.prehandler.KeelPlatformHandler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

import javax.annotation.Nonnull;

/**
 * @since 3.1.10
 */
public class KeelWebReceptionistRequestEventLog extends KeelEventLogImpl {
    public KeelWebReceptionistRequestEventLog(@Nonnull String topic, RoutingContext routingContext) {
        super(KeelLogLevel.INFO, topic);
        this.toJsonObject()
                .put("request", new JsonObject()
                        .put("request_id", routingContext.get(KeelPlatformHandler.KEEL_REQUEST_ID))
                        .put("method", routingContext.request().method().name())
                        .put("path", routingContext.request().path())
                        .put("handler", this.getClass().getName())
                );
    }
}
