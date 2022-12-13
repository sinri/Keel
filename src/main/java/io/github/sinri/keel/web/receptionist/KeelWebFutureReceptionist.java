package io.github.sinri.keel.web.receptionist;

import io.github.sinri.keel.web.ApiMeta;
import io.vertx.core.Future;
import io.vertx.ext.web.RoutingContext;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * @since 2.9.2
 * @since 2.9.2 add authorization with privileges
 */
abstract public class KeelWebFutureReceptionist extends KeelWebReceptionist {

    public KeelWebFutureReceptionist(RoutingContext routingContext) {
        super(routingContext);
    }

    @Override
    public void handle() {
        Future.succeededFuture()
                .compose(v -> privileges())
                .compose(this::authorize)
                .compose(v -> handleForFuture())
                .andThen(ar -> {
                    if (ar.failed()) {
                        this.respondOnFailure(ar.cause());
                    } else {
                        this.respondOnSuccess(ar.result());
                    }
                });
    }

    /**
     * @return Privilege Set
     * @since 2.9.4
     */
    protected Future<Collection<String>> privileges() {
        ApiMeta apiMeta = getApiMeta();
        if (apiMeta == null) {
            return Future.succeededFuture(null);
        } else {
            String[] privileges = apiMeta.privileges();
            if (privileges == null) {
                return Future.succeededFuture(null);
            }
            Set<String> s = new HashSet<>(Arrays.asList(privileges));
            return Future.succeededFuture(s);
        }
    }

    /**
     * @param privileges Privilege Set
     * @return future of success or failed
     * @since 2.9.4
     */
    protected Future<Void> authorize(Collection<String> privileges) {
        return Future.succeededFuture();
    }

    abstract protected Future<Object> handleForFuture();
}
