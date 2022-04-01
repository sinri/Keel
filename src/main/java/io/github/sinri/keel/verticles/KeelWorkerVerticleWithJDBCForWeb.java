package io.github.sinri.keel.verticles;

import io.vertx.ext.web.RoutingContext;

/**
 * @param <R>
 * @since 1.14
 */
abstract public class KeelWorkerVerticleWithJDBCForWeb<R> extends KeelWorkerVerticleWithJDBC<R> {
    protected final RoutingContext routingContext;

    public KeelWorkerVerticleWithJDBCForWeb(RoutingContext routingContext) {
        super();
        this.routingContext = routingContext;
    }
}
