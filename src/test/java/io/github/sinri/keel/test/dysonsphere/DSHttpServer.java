package io.github.sinri.keel.test.dysonsphere;

import io.github.sinri.keel.web.http.KeelHttpServer;
import io.github.sinri.keel.web.http.fastdocs.KeelFastDocsKit;
import io.github.sinri.keel.web.http.receptionist.KeelWebReceptionist;
import io.github.sinri.keel.web.http.receptionist.KeelWebReceptionistKit;
import io.vertx.ext.web.Router;

public class DSHttpServer extends KeelHttpServer {
    @Override
    protected void configureRoutes(Router router) {
        KeelFastDocsKit.installToRouter(
                router,
                "/fastdocs/",
                "web_root/fastdocs/",
                "Dyson Sphere FastDocs",
                "Copyright 2022 Sinri Edogawa"
        );

        new KeelWebReceptionistKit<KeelWebReceptionist>(KeelWebReceptionist.class, router)
                .loadPackage("io.github.sinri.keel.test.dysonsphere.receptionist");
    }
}
