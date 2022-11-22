package io.github.sinri.keel.test.servant;

import io.github.sinri.keel.Keel;
import io.github.sinri.keel.test.SharedTestBootstrap;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;

import java.util.function.Supplier;

public class WorkerVerticleTest {
    public static void main(String[] args) {
        SharedTestBootstrap.initialize(v0 -> {
            Keel.getVertx().deployVerticle(V1.class, new DeploymentOptions()
                    .setWorker(true));
        });
    }

    public static class V1 extends AbstractVerticle {
        @Override
        public void start() throws Exception {
            super.start();
            Keel.outputLogger().info("START");
            for (int i = 0; i < 5; i++) {
                int finalI = i;
                Keel.callFutureUntil(new Supplier<Future<Boolean>>() {
                    @Override
                    public Future<Boolean> get() {
                        route(finalI);
                        routeAsAnotherVerticle(finalI);
                        return Keel.callFutureSleep(1L)
                                .compose(v -> {
                                    return Future.succeededFuture(false);
                                });
                    }
                });
            }
        }

        private void route(int i) {
            Keel.outputLogger().info("No." + i + " ROUTING start");
            try {
                Thread.sleep(1000L);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            Keel.outputLogger().info("No." + i + " ROUTING end");
        }

        private void routeAsAnotherVerticle(int i) {
            Keel.getVertx().deployVerticle(V2.class, new DeploymentOptions()
                    .setConfig(new JsonObject().put("i", i)));
        }
    }

    public static class V2 extends AbstractVerticle {
        @Override
        public void start() throws Exception {
            super.start();
            Future.succeededFuture()
                    .andThen(v -> {
                        Integer i = config().getInteger("i");
                        route(i);
                        Keel.getVertx().undeploy(deploymentID());
                    });
        }

        private void route(int i) {
            Keel.outputLogger().info("No." + i + " ROUTING start");
            try {
                Thread.sleep(1000L);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            Keel.outputLogger().info("No." + i + " ROUTING end");
        }
    }
}
