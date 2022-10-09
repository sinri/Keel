package io.github.sinri.keel.web.service;

import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;

/**
 * @since 2.8.1
 */
public abstract class KeelWebRequestPreHandler implements Handler<RoutingContext> {
    private RoutingContext routingContext;

    public RoutingContext getRoutingContext() {
        return routingContext;
    }

    @Override
    public void handle(RoutingContext routingContext) {
        this.routingContext = routingContext;
        Future.succeededFuture()
                .compose(v -> handleRequest(routingContext))
                .onSuccess(v -> routingContext.next())
                .onFailure(routingContext::fail);
    }

    abstract protected Future<Object> handleRequest(RoutingContext routingContext);
}
