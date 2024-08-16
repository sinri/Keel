package io.github.sinri.keel.web.http.prehandler;

import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.User;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.AuthenticationHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


/**
 * Tell who the user is, if not a legal user, fail the request with RequestDenied.
 *
 * @since 2.9.2
 * @since 3.0.0 TEST PASSED
 */
abstract public class KeelAuthenticationHandler implements AuthenticationHandler {

    @Override
    public void handle(RoutingContext routingContext) {
        // BEFORE ASYNC PAUSE
        routingContext.request().pause();
        Future.succeededFuture()
                .compose(v -> handleRequest(routingContext))
                .andThen(ar -> {
                    if (ar.failed()) {
                        routingContext.fail(ar.cause());
                        return;
                    }

                    AuthenticateResult authenticateResult = ar.result();
                    if (!authenticateResult.isLegalRequest()) {
                        authenticateResult.failRequest(routingContext);
                        return;
                    }

                    routingContext.setUser(authenticateResult.authenticatedUser());

                    // RESUME
                    routingContext.request().resume();
                    // NEXT
                    routingContext.next();
                });
    }

    abstract protected Future<AuthenticateResult> handleRequest(RoutingContext routingContext);

    public interface AuthenticateResult {

        static AuthenticateResult createAuthenticatedResult() {
            return new AuthenticateResultImpl();
        }

        static AuthenticateResult createAuthenticatedResult(JsonObject principle) {
            return new AuthenticateResultImpl(principle);
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

        default JsonObject authenticatedPrinciple() {
            return new JsonObject();
        }

        default User authenticatedUser() {
            return User.create(authenticatedPrinciple());
        }

//        default AuthenticateResult setSessionExpire(long expireTimestamp) {
//            // exp is expected as (System.currentTimeMillis() / 1000);
//            //  or new Date().getTime() / 1000
//            if (expireTimestamp > 1660000000000L) {
//                expireTimestamp = expireTimestamp / 1000;
//            }
//            this.authenticatedUser().attributes().put("exp", expireTimestamp);
//            return this;
//        }
    }

    private static class AuthenticateResultImpl implements AuthenticateResult {

        final boolean legal;
        final Throwable throwable;
        final int respondStatusCode;
        /**
         * @since 3.2.10 it became non-null.
         */
        @NotNull
        final JsonObject principle;


        public AuthenticateResultImpl() {
            this.legal = true;
            this.throwable = null;
            this.respondStatusCode = 401;
            this.principle = new JsonObject();
        }

        public AuthenticateResultImpl(@NotNull JsonObject principle) {
            this.legal = true;
            this.throwable = null;
            this.respondStatusCode = 401;
            this.principle = principle;
        }

        public AuthenticateResultImpl(Throwable throwable) {
            this.legal = false;
            this.throwable = throwable;
            this.respondStatusCode = 401;
            this.principle = new JsonObject();
        }

        public AuthenticateResultImpl(int respondStatusCode, Throwable throwable) {
            this.legal = false;
            this.throwable = throwable;
            this.respondStatusCode = respondStatusCode;
            this.principle = new JsonObject();
        }

        @Override
        public boolean isLegalRequest() {
            return legal;
        }

        @Nullable
        @Override
        public Throwable failure() {
            return throwable;
        }

        @NotNull
        @Override
        public JsonObject authenticatedPrinciple() {
            return principle;
        }
    }
}
