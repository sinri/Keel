package io.github.sinri.keel.web;

import io.vertx.core.Future;
import io.vertx.ext.web.RoutingContext;

import java.lang.reflect.Method;

/**
 * @since 1.1
 * @since 1.10 became abstract
 */
public abstract class KeelWebRequestFilter {
    protected Method method;

    public String filterName() {
        return getClass().getName();
    }

    public KeelWebRequestFilter setTargetMethod(Method method) {
        this.method = method;
        return this;
    }

    abstract public Future<Void> shouldHandleThisRequest(RoutingContext ctx);
}
