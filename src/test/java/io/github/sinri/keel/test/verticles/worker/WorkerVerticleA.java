package io.github.sinri.keel.test.verticles.worker;

import io.github.sinri.keel.Keel;
import io.github.sinri.keel.core.logger.KeelLogger;
import io.github.sinri.keel.test.SharedTestBootstrap;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;

public class WorkerVerticleA extends AbstractVerticle {
    private final KeelLogger logger = Keel.outputLogger("WorkerVerticleA");

    public WorkerVerticleA() {
        super();
    }

    public static void main(String[] args) {
        SharedTestBootstrap.initialize();

        for (var i = 0; i < 10; i++) {
            Keel.getVertx()
                    .deployVerticle(
                            WorkerVerticleA.class,
                            new DeploymentOptions()
                                    .setWorker(true)
                                    .setConfig(new JsonObject()
                                            .put("index", i)
                                    )
                    )
                    .compose(x -> {
                        Keel.outputLogger("main").info("deployVerticle compose: " + x);
                        return Future.succeededFuture();
                    });
        }

        Keel.getVertx().setTimer(10000L, timerID -> {
            Keel.outputLogger("main").info("closed time");
            Keel.getVertx().close();
        });
    }

    private void syncBody() {
        this.logger.info("WorkerVerticleA::syncBody started");
        this.logger.info("WorkerVerticleA::syncBody config", config());
        try {
            Thread.sleep(3000L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        this.logger.info("WorkerVerticleA::syncBody ended");
    }

    @Override
    public void start() throws Exception {
        super.start();
        this.logger.setCategoryPrefix("VD#" + this.deploymentID());
        this.logger.info("WorkerVerticleA::start");
        syncBody();
        //stop();
    }

    @Override
    public void stop() throws Exception {
        super.stop();
        this.logger.info("WorkerVerticleA::stop");
    }
}
