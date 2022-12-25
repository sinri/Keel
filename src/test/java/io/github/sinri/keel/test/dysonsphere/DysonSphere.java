package io.github.sinri.keel.test.dysonsphere;

import io.github.sinri.keel.facade.Keel;
import io.github.sinri.keel.test.SharedTestBootstrap;
import io.github.sinri.keel.web.http.KeelHttpServer;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.json.JsonObject;

public class DysonSphere {
    private final Keel keel;

    private DysonSphere(Keel keel) {
        this.keel = keel;
    }

    public static void main(String[] args) {
        SharedTestBootstrap.bootstrap(keel -> {
            DysonSphere dysonSphere = new DysonSphere(keel);
            dysonSphere.startHttpServer();
        });
    }

    private void startHttpServer() {
        keel.deployKeelVerticle(DSHttpServer.class, new DeploymentOptions()
                .setConfig(new JsonObject()
                        .put(KeelHttpServer.CONFIG_HTTP_SERVER_PORT, 8080)
                )
        );
    }
}
