package io.github.sinri.keel.test.dysonsphere;

import io.github.sinri.keel.facade.Keel;
import io.github.sinri.keel.test.SharedTestBootstrap;
import io.github.sinri.keel.web.http.KeelHttpServer;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.json.JsonObject;

public class DysonSphere {

    public static void main(String[] args) {
        SharedTestBootstrap.bootstrap(v0 -> {
            DysonSphere dysonSphere = new DysonSphere();
            dysonSphere.startHttpServer();
        });
    }

    private void startHttpServer() {
        Keel.getVertx().deployVerticle(DSHttpServer.class, new DeploymentOptions()
                .setConfig(new JsonObject()
                        .put(KeelHttpServer.CONFIG_HTTP_SERVER_PORT, 8080)
                )
        );
    }
}
