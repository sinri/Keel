package io.github.sinri.keel.web.service;

import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;

/**
 * @since 2.9
 *
 */
@Deprecated(since = "2.9")
public abstract class KeelWebRequestPreHandler implements Handler<RoutingContext> {
    private RoutingContext routingContext;
    private int statusCode;
    private String statusMessage;

    public RoutingContext getRoutingContext() {
        return routingContext;
    }

    /**
     * If using Future would not be easy, override it.
     */
    @Override
    public void handle(RoutingContext routingContext) {
        this.routingContext = routingContext;
        Future.succeededFuture()
                .compose(v -> handleRequest(routingContext))
                .andThen(ar -> {
                    if (ar.succeeded()) {
                        routingContext.next();
                    } else {
                        var throwable = ar.cause();
                        if (throwable instanceof RequestDenied) {
                            routingContext.fail(((RequestDenied) throwable).getCode(), throwable);
                        } else {
                            routingContext.fail(throwable);
                        }
                    }
                });
    }

    /**
     * @return future, if failed, use RequestDenied as cause.
     */
    abstract protected Future<Void> handleRequest(RoutingContext routingContext);

    public static class RequestDenied extends Exception {
        private final int code;

        public RequestDenied(int code, String message, Throwable cause) {
            super(message, cause);
            this.code = code;
        }

        public RequestDenied(int code, String message) {
            super(message);
            this.code = code;
        }

        public RequestDenied(int code) {
            this(code, "Request Denied");
        }

        public RequestDenied() {
            this(403);
        }

        public int getCode() {
            return code;
        }
    }
}
