package io.github.sinri.keel.test.dysonsphere;

import io.github.sinri.keel.logger.event.center.KeelOutputEventLogCenter;
import io.github.sinri.keel.web.http.KeelHttpServer;
import io.github.sinri.keel.web.http.blackbox.KeelBlackBox;
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
                "Copyright 2022 Sinri Edogawa",
                KeelOutputEventLogCenter.getInstance().createLogger("FastDocs")
        );

        KeelBlackBox.installToRouter(
                "/blackbox/",
                "/Users/leqee/code/Keel/log",
                router
        );

        new KeelWebReceptionistKit<KeelWebReceptionist>(KeelWebReceptionist.class, router)
                .loadPackage("io.github.sinri.keel.test.dysonsphere.receptionist");
    }
}
