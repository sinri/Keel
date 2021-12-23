package io.github.sinri.keel.web;

import io.vertx.core.Future;
import io.vertx.ext.web.RoutingContext;

/**
 * @since 1.1
 */
public class KeelWebRequestFilter {
    public String filterName() {
        return getClass().getName();
    }

    public Future<Void> shouldHandleThisRequest(RoutingContext ctx) {
        // if you should not handle it, throw!
        return Future.succeededFuture();
    }
}
