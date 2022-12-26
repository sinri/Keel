package io.github.sinri.keel.web.http.handler;

import io.github.sinri.keel.logger.event.KeelEventLogger;
import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;

public interface KeelHttpHandler extends Handler<RoutingContext> {

    KeelEventLogger getLogger();
}
