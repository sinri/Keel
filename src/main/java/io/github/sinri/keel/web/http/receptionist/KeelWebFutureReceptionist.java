package io.github.sinri.keel.web.http.receptionist;

import io.vertx.core.Future;
import io.vertx.ext.web.RoutingContext;

/**
 * @since 2.9.2
 * @since 2.9.2 add authorization with privileges
 * @since 3.0.0 TEST PASSED
 */
abstract public class KeelWebFutureReceptionist extends KeelWebReceptionist {

    public KeelWebFutureReceptionist(RoutingContext routingContext) {
        super(routingContext);
    }

    @Override
    public void handle() {
        // since 3.1.5 add a starting log
        getLogger().info(log -> log.message("TO HANDLE REQUEST"));

        Future.succeededFuture()
                .compose(v -> handleForFuture())
                .andThen(ar -> {
                    if (ar.failed()) {
                        this.respondOnFailure(ar.cause());
                    } else {
                        this.respondOnSuccess(ar.result());
                    }
                });
    }

    abstract protected Future<Object> handleForFuture();
}
