package io.github.sinri.keel.test.web;

import io.github.sinri.keel.Keel;
import io.github.sinri.keel.test.SharedTestBootstrap;
import io.github.sinri.keel.test.web.receptionist.ReceptionistA;
import io.github.sinri.keel.test.web.receptionist.RootPathReceptionist;
import io.github.sinri.keel.web.KeelHttpServer;
import io.github.sinri.keel.web.KeelWebRequestReceptionist;
import io.vertx.core.http.HttpServerOptions;

public class TestWebServiceV2 {
    public static void main(String[] args) {
        SharedTestBootstrap.initialize();
        KeelHttpServer khs = new KeelHttpServer(Keel.getVertx(), new HttpServerOptions().setPort(14000), true);

        // khs.getRouter().get("/").handler(ctx -> new RootPathReceptionist(ctx).deployMe());
        KeelWebRequestReceptionist.registerRoute(
                khs.getRouter().get("/"),
                RootPathReceptionist.class,
                true
        );

        KeelWebRequestReceptionist.registerRoute(
                khs.getRouter().get("/a"),
                ReceptionistA.class,
                true
        );

        khs.listen();
    }
}
