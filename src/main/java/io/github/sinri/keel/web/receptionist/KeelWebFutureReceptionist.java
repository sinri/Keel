package io.github.sinri.keel.web.receptionist;

import io.vertx.core.Future;
import io.vertx.ext.web.RoutingContext;

/**
 * @since 2.9.2
 */
abstract public class KeelWebFutureReceptionist extends KeelWebReceptionist {

    public KeelWebFutureReceptionist(RoutingContext routingContext) {
        super(routingContext);
    }

    @Override
    public void handle() {
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
