package io.github.sinri.keel.web.service;

import io.vertx.core.Future;

/**
 * @since 2.9
 */
public abstract class KeelWebRequestFutureHandler extends KeelWebRequestHandler {


    abstract protected Future<Object> handleRequestForFuture();

    @Override
    public final void handleRequest() {
        Future.succeededFuture()
                .compose(v -> handleRequestForFuture())
                .onSuccess(this::respondOnSuccess)
                .onFailure(this::respondOnFailure);
    }
}
