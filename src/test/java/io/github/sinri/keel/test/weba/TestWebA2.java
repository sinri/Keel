package io.github.sinri.keel.test.weba;

import io.github.sinri.keel.Keel;
import io.github.sinri.keel.core.controlflow.FutureSleep;
import io.github.sinri.keel.mysql.KeelMySQLKit;
import io.github.sinri.keel.test.SharedTestBootstrap;
import io.github.sinri.keel.web.KeelHttpServer;
import io.github.sinri.keel.web.service.KeelWebRequestPromiseHandler;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.json.JsonObject;

import java.util.Objects;

public class TestWebA2 {
    public static void main(String[] args) {
        SharedTestBootstrap.initialize();

        new KeelHttpServer(new HttpServerOptions().setPort(8822), true)
                .configureRoutes(router -> {
                    router.get("/:a").handler(new X());
                })
                .listen();
    }

    public static class X extends KeelWebRequestPromiseHandler {

        @Override
        protected long timeout() {
            return 1000L;
        }

        @Override
        protected void handleRequestForFuture(Promise<Object> promise) {
            String a = getRoutingContext().pathParam("a");
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
    }
}
