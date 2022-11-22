package io.github.sinri.keel.test.weba;

import io.github.sinri.keel.Keel;
import io.github.sinri.keel.core.controlflow.FutureSleep;
import io.github.sinri.keel.core.logger.KeelLogger;
import io.github.sinri.keel.mysql.KeelMySQLKit;
import io.github.sinri.keel.test.SharedTestBootstrap;
import io.github.sinri.keel.web.KeelHttpServer;
import io.github.sinri.keel.web.service.KeelWebRequestFutureHandler;
import io.github.sinri.keel.web.service.KeelWebRequestPromiseHandler;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

import java.util.Objects;

public class TestWebA2 {
    public static void main(String[] args) {
        SharedTestBootstrap.initialize(v0 -> {
            new KeelHttpServer(new HttpServerOptions().setPort(8822), true)
                    .configureRoutes(router -> {
                        router.get("/:a").handler(new X());
                        // todo confirm it.
                        router.get("/y/error").handler(new Y());
                        router.route().last().failureHandler(ctx -> {
                            Throwable failure = ctx.failure();
                            Keel.outputLogger().exception(failure);
                            ctx.response().setStatusCode(500).setStatusMessage(failure.toString()).end();
                        });
                    })
                    .listen();
        });


    }

    public static class X extends KeelWebRequestPromiseHandler {

        @Override
        protected long timeout() {
            return 1000L;
        }

        @Override
        protected void handleRequestForFuture(RoutingContext routingContext, Promise<Object> promise) {
            String a = routingContext.pathParam("a");
            JsonObject jsonObject = new JsonObject()
                    .put("a", a)
                    .put("now", KeelMySQLKit.nowAsMySQLDatetime());
            if (Objects.equals(a, "a")) {
                promise.complete(jsonObject);
            }
            if (Objects.equals(a, "b")) {
                promise.fail(jsonObject.toString());
            } else {
                FutureSleep.call(2000).andThen(v -> {
                    Keel.outputLogger().info("2s passed... go");
                    try {
                        promise.complete(jsonObject);
                        Keel.outputLogger().info("2s passed... ok");
                    } catch (IllegalStateException e) {
                        Keel.outputLogger().exception(e);
                    }
                });
            }
        }

        @Override
        public KeelLogger createLogger(RoutingContext routingContext) {
            return Keel.outputLogger();
        }
    }

    public static class Y extends KeelWebRequestFutureHandler {

        @Override
        protected Future<Object> handleRequestForFuture(RoutingContext routingContext) {
            return Future.failedFuture(new RuntimeException("123"));
        }

        @Override
        public KeelLogger createLogger(RoutingContext routingContext) {
            return Keel.outputLogger();
        }
    }
}
