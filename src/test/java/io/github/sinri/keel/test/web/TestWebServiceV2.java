package io.github.sinri.keel.test.web;

import io.github.sinri.keel.Keel;
import io.github.sinri.keel.core.logger.KeelLogger;
import io.github.sinri.keel.test.SharedTestBootstrap;
import io.github.sinri.keel.test.web.receptionist.ReceptionistA;
import io.github.sinri.keel.test.web.receptionist.RootPathReceptionist;
import io.github.sinri.keel.test.web.ws.WebSocketTest2;
import io.github.sinri.keel.web.KeelHttpServer;
import io.github.sinri.keel.web.blackbox.BlackBox;
import io.github.sinri.keel.web.legacy.KeelWebRequestReceptionist;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.ext.web.handler.StaticHandler;

public class TestWebServiceV2 {
    public static void main(String[] args) {
        SharedTestBootstrap.initialize();

        KeelLogger routerLogger = Keel.standaloneLogger("router");

        KeelHttpServer khs = new KeelHttpServer(new HttpServerOptions().setPort(14000), true);

        new BlackBox("/blackbox", "/Users/leqee/code/Keel/log")
                .registerToRoute(khs.getRouter());

        // khs.getRouter().get("/").handler(ctx -> new RootPathReceptionist(ctx).deployMe());
        KeelWebRequestReceptionist.registerRoute(
                khs.getRouter().get("/"),
                RootPathReceptionist.class,
                true,
                routerLogger
        );

        KeelWebRequestReceptionist.registerRoute(
                khs.getRouter().get("/a"),
                ReceptionistA.class,
                true,
                routerLogger
        );

        khs.getRouter().get("/ws/page")
                .handler(StaticHandler.create("web_root/websocket"));

        WebSocketTest2.upgradeFromHttp(khs.getRouter().get("/ws/api"), WebSocketTest2.class, routerLogger, null);

//        khs.websocket(webSocket -> {
//            WebSocketTest.handle(webSocket, WebSocketTest.class);
//        });

        khs.listen();
    }
}
