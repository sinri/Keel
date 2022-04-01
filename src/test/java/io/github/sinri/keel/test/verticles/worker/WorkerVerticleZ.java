package io.github.sinri.keel.test.verticles.worker;

import io.github.sinri.keel.Keel;
import io.github.sinri.keel.core.controlflow.FutureForRange;
import io.github.sinri.keel.test.SharedTestBootstrap;
import io.github.sinri.keel.verticles.async.KeelWorkerVerticleWithMySQL;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;

import java.util.Objects;

public class WorkerVerticleZ extends KeelWorkerVerticleWithMySQL {

    public static void main(String[] args) {
        SharedTestBootstrap.initialize();

        for (var i = 0; i < 10; i++) {
            WorkerVerticleZ z = new WorkerVerticleZ();
            z.deployAndRun(new JsonObject().put("index", i))
                    .compose(deploymentID -> {
                        Keel.outputLogger("main").info("deployed " + deploymentID);
                        return Future.succeededFuture();
                    });
        }

        Keel.getVertx().setTimer(10000L, timerID -> {
            Keel.outputLogger("main").info("closed time");
            Keel.getVertx().close();
        });
    }

    private Future<Void> step(Integer current) {
        getLogger().info("step " + current);
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return Future.succeededFuture().compose(v -> {
            Integer indexFromCurrentContext = Keel.getVertx().getOrCreateContext().get("index");
            Integer indexFromVerticleContext = context.get("index");
            if (!Objects.equals(indexFromCurrentContext, indexFromVerticleContext)) {
                getLogger().warning("DIFF", new JsonObject()
                        .put("indexFromCurrentContext", indexFromCurrentContext)
                        .put("indexFromVerticleContext", indexFromVerticleContext)
                );
            }
            return Future.succeededFuture();
        });
    }

    @Override
    public Future<Void> runInTransaction() {
        //Keel.getVertx().getOrCreateContext()
        context.put("index", config().getInteger("index"));
        return new FutureForRange(10)
                .run(this::step)
                .compose(f -> Future.succeededFuture());
    }
}
