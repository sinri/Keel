package io.github.sinri.Keel.test.web;

import io.github.sinri.Keel.Keel;
import io.github.sinri.Keel.web.KeelControllerStyleRouterKit;
import io.github.sinri.Keel.web.KeelHttpServer;
import io.vertx.core.VertxOptions;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.ext.web.handler.BodyHandler;

public class WebTestMainV2 {
    public static void main(String[] args) {
        Keel.initializeVertx(new VertxOptions().setWorkerPoolSize(2));
        Keel.loadPropertiesFromFile("test.properties");

        KeelHttpServer khs = new KeelHttpServer(Keel.getVertx(), new HttpServerOptions().setPort(14000), true);

        // static route
        khs.getRouter().get("/").handler(ctx -> ctx.response().end("HERE IS ROOT"));
        // automatic Controller - Method route
        KeelControllerStyleRouterKit keelControllerStyleRouterKit = new KeelControllerStyleRouterKit("io.github.sinri.Keel.test.web.controller");
        khs.getRouter()
                .route()
                .handler(BodyHandler.create())
                .handler(keelControllerStyleRouterKit::processRouterRequest);

        khs.listen();
    }
}
