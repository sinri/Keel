package io.github.sinri.keel.web.service;

import io.vertx.core.Future;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.AuthenticationHandler;

/**
 * Tell who the user is, if not a legal user, fail the request with RequestDenied.
 *
 * @param <T> The AuthenticateResult Implementation.
 * @since 2.9.2
 */
abstract public class KeelAuthenticationHandler<T extends KeelAuthenticationHandler.AuthenticateResult> implements AuthenticationHandler {

    @Override
    public void handle(RoutingContext routingContext) {
        Future.succeededFuture()
                .compose(v -> handleRequest(routingContext))
                .andThen(ar -> {
                    if (ar.failed()) {
                        routingContext.fail(ar.cause());
                        return;
                    }

                    T authenticateResult = ar.result();
                    if (!authenticateResult.isLegalRequest()) {
                        authenticateResult.failRequest(routingContext);
                        return;
                    }

                    routingContext.next();
                });
    }

    abstract protected Future<T> handleRequest(RoutingContext routingContext);

    public interface AuthenticateResult {

        static AuthenticateResult createAuthenticatedResult() {
            return new AuthenticateResultImpl();
        }

        static AuthenticateResult createAuthenticateFailedResult(Throwable throwable) {
            return new AuthenticateResultImpl(throwable);
        }

        static AuthenticateResult createAuthenticateFailedResult(int respondStatusCode, Throwable throwable) {
            return new AuthenticateResultImpl(respondStatusCode, throwable);
        }

        boolean isLegalRequest();

        default int statusCodeToFailRequest() {
            return 401;
        }

        default Throwable failure() {
            return new Exception("Request Denied");
        }

        default void failRequest(RoutingContext routingContext) {
            routingContext.fail(statusCodeToFailRequest(), failure());
        }
    }

    private static class AuthenticateResultImpl implements AuthenticateResult {

        final boolean legal;
        final Throwable throwable;
        final int respondStatusCode;


        public AuthenticateResultImpl() {
            this.legal = true;
            this.throwable = null;
            this.respondStatusCode = 401;
        }

        public AuthenticateResultImpl(Throwable throwable) {
            this.legal = false;
            this.throwable = throwable;
            this.respondStatusCode = 401;
        }

        public AuthenticateResultImpl(int respondStatusCode, Throwable throwable) {
            this.legal = false;
            this.throwable = throwable;
            this.respondStatusCode = respondStatusCode;
        }

        @Override
        public boolean isLegalRequest() {
            return legal;
        }

        @Override
        public Throwable failure() {
            return throwable;
        }
    }
}
