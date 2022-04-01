package io.github.sinri.keel.verticles.sync;

import io.vertx.ext.web.RoutingContext;

/**
 * @param <R>
 * @since 1.14
 */
@Deprecated
abstract public class KeelSyncWorkerVerticleWithJDBCForWeb<R> extends KeelSyncWorkerVerticleWithJDBC<R> {
    protected final RoutingContext routingContext;

    public KeelSyncWorkerVerticleWithJDBCForWeb(RoutingContext routingContext) {
        super();
        this.routingContext = routingContext;
    }
}
