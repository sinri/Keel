package io.github.sinri.keel.test.web;

import io.github.sinri.keel.Keel;
import io.github.sinri.keel.test.SharedTestBootstrap;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;

public class VerticleInsideRuntimeErrorTest {
    public static void main(String[] args) {
        SharedTestBootstrap.initialize();

        Keel.getVertx().deployVerticle(V.class, new DeploymentOptions())
                .compose(deploymentID -> {
                    System.out.println("deploymentID: " + deploymentID);
                    return Future.succeededFuture();
                })
                .onFailure(throwable -> {
                    System.out.println("deployment error: " + throwable);
                });
    }

    public static class V extends AbstractVerticle {
        @Override
        public void start() throws Exception {
            super.start();

            Future.succeededFuture()
                    .compose(v -> {
                        System.out.println("step 1");
                        return Future.succeededFuture(1);
                    })
                    .compose(x -> {
                        System.out.println("step 2: " + x);
                        if (x == 1) {
                            throw new RuntimeException("one");
                        }
                        return Future.succeededFuture(x + 1);
                    })
                    .onComplete(r -> {
                        System.out.println("step 3: ");
                        if (r.succeeded()) {
                            System.out.println("ok " + r.result());
                        } else {
                            System.out.println("ko " + r.cause().getMessage());
                        }
                    });
        }
    }
}
