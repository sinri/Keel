package io.github.sinri.keel.test.web;

import io.github.sinri.keel.Keel;
import io.github.sinri.keel.test.SharedTestBootstrap;
import io.github.sinri.keel.web.KeelControllerStyleRouterKit;
import io.github.sinri.keel.web.KeelHttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.ext.web.handler.BodyHandler;

public class WebTestMainV2 {
    public static void main(String[] args) {
        SharedTestBootstrap.initialize();

        KeelHttpServer khs = new KeelHttpServer(Keel.getVertx(), new HttpServerOptions().setPort(14000), true);

        // static route
        khs.getRouter().get("/").handler(ctx -> ctx.response().end("HERE IS ROOT"));
        // automatic Controller - Method route
        KeelControllerStyleRouterKit keelControllerStyleRouterKit = new KeelControllerStyleRouterKit("io.github.sinri.keel.test.web.controller");
        keelControllerStyleRouterKit.setLogger(Keel.outputLogger("KeelControllerStyleRouterKit"));
        khs.getRouter()
                .route()
                .handler(BodyHandler.create())
                .handler(keelControllerStyleRouterKit::processRouterRequest);

        khs.listen();
    }
}
