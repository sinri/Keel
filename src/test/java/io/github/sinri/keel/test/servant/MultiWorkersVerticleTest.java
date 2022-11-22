package io.github.sinri.keel.test.servant;

import io.github.sinri.keel.Keel;
import io.github.sinri.keel.test.SharedTestBootstrap;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;

/**
 * 本类验证结果如下：
 * 一个worker verticle以多instance部署运行，
 * 其结果表现与独立部署多个verticle类似，但verticle的deployment id相同。
 */
public class MultiWorkersVerticleTest {
    public static void main(String[] args) {
        SharedTestBootstrap.initialize(v0 -> {
            Keel.outputLogger().notice("READY");
            Future.succeededFuture()
                    .andThen(v1 -> {
                        Keel.outputLogger().notice("v1: " + v1.succeeded());
                        Keel.getVertx()
                                .deployVerticle(V1.class, new DeploymentOptions()
                                        .setWorker(true)
                                        .setInstances(2)
                                )
                                .andThen(ar -> {
                                    if (ar.failed()) {
                                        Keel.outputLogger().exception(ar.cause());
                                    } else {
                                        Keel.outputLogger().info(ar.result());
                                    }
                                });
                    });

        });
    }

    public static class V1 extends AbstractVerticle {
        @Override
        public void start() throws Exception {
            super.start();

            getVertx().setPeriodic(5000L, timerID -> {
                Keel.outputLogger().info("RUN");
            });
        }
    }
}
