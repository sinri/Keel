package io.github.sinri.keel.test.verticles.worker;

import io.github.sinri.keel.Keel;
import io.github.sinri.keel.core.controlflow.FutureForRange;
import io.github.sinri.keel.mysql.KeelMySQLKit;
import io.github.sinri.keel.mysql.statement.UpdateStatement;
import io.github.sinri.keel.mysql.statement.WriteIntoStatement;
import io.github.sinri.keel.test.SharedTestBootstrap;
import io.github.sinri.keel.verticles.async.KeelWorkerVerticleWithMySQL;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;

public class WorkerVerticleY extends KeelWorkerVerticleWithMySQL {

    public static void main(String[] args) {
        SharedTestBootstrap.initialize();

        for (var i = 0; i < 100; i++) {
            new WorkerVerticleY()
                    .deployAndRun(new JsonObject().put("index", i))
                    .compose(deploymentID -> {
                        Keel.outputLogger("main").info("deployed " + deploymentID);
                        return Future.succeededFuture();
                    });
        }

        Keel.getVertx().setTimer(30000L, timerID -> {
            Keel.outputLogger("main").info("closed time");
            Keel.getVertx().close();
        });

        /*
        select verticle_deployment_id,count(distinct uuid) as usedUUID
from test_context
group by verticle_deployment_id
having usedUUID>1
         */
    }

    private Future<Void> step(Integer i) {
        //        SqlConnection sqlConnection = KeelMySQLKit.getSqlConnectionFromVerticleContext();
        getLogger().info("SqlConnection UUID: " + KeelMySQLKit.getSqlConnectionUUIDFromVerticleContext());
        return new WriteIntoStatement()
                .intoTable("test_context")
                .macroWriteOneRowWithJsonObject(new JsonObject()
                        .put("verticle_deployment_id", deploymentID())
                        .put("thread_id", Thread.currentThread().getId())
                        .put("uuid", KeelMySQLKit.getSqlConnectionUUIDFromVerticleContext())
                        .put("step", config().getInteger("index"))
                        .put("value", "INIT")
                )
                .executeForLastInsertedID(KeelMySQLKit.getSqlConnectionFromVerticleContext())
                .compose(id -> {
                    getLogger().info("SqlConnection UUID: " + KeelMySQLKit.getSqlConnectionUUIDFromVerticleContext());
                    getLogger().info("id: " + id);
                    try {
                        Thread.sleep(100L);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    return new UpdateStatement()
                            .table("test_context")
                            .setWithValue("value", "DONE")
                            .where(conditionsComponent -> conditionsComponent
                                    .comparison(compareCondition -> compareCondition
                                            .filedEqualsValue("id", id))
                            )
                            .executeForAffectedRows(KeelMySQLKit.getSqlConnectionFromVerticleContext());
                })
                .compose(afx -> {
                    getLogger().info("SqlConnection UUID: " + KeelMySQLKit.getSqlConnectionUUIDFromVerticleContext());
                    getLogger().info("afx: " + afx);
                    return Future.succeededFuture();
                });
    }

    @Override
    public Future<Void> runInTransaction() {
        return new FutureForRange(100).run(this::step);
    }
}
