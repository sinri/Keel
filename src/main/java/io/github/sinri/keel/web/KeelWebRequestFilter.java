package io.github.sinri.keel.web;

import io.vertx.core.Future;
import io.vertx.ext.web.RoutingContext;

/**
 * @since 1.1
 * @since 1.10 became abstract
 */
public abstract class KeelWebRequestFilter {
    public String filterName() {
        return getClass().getName();
    }

    abstract public Future<Void> shouldHandleThisRequest(RoutingContext ctx);
}
