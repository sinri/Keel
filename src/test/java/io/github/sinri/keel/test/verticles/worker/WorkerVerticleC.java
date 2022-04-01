package io.github.sinri.keel.test.verticles.worker;

import io.github.sinri.keel.Keel;
import io.github.sinri.keel.mysql.statement.SelectStatement;
import io.github.sinri.keel.mysql.statement.UpdateStatement;
import io.github.sinri.keel.test.SharedTestBootstrap;
import io.github.sinri.keel.verticles.KeelWorkerVerticleWithJDBC;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;

public class WorkerVerticleC extends KeelWorkerVerticleWithJDBC<Integer> {
    public static void main(String[] args) {
        SharedTestBootstrap.initialize();

        for (var i = 0; i < 10; i++) {
            new WorkerVerticleC()
                    .deployAndRun(new JsonObject().put("index", i))
                    .compose(x -> {
                        Keel.outputLogger("main").info("deployVerticle compose i => " + x);
                        return Future.succeededFuture();
                    })
                    .onFailure(throwable -> {
                        Keel.outputLogger("main").exception(throwable);
                    });
        }

        Keel.getVertx().setTimer(10000L, timerID -> {
            Keel.outputLogger("main").info("closed time");
            Keel.getVertx().close();
        });
    }

    @Override
    protected Integer transactionBody() throws Exception {
        var row = new SelectStatement()
                .columnAsExpression("*")
                .from("test_a")
                .where(conditionsComponent -> conditionsComponent
                        .comparison(compareCondition -> compareCondition
                                .filedEqualsValue("test_a_id", 1)))
                .limit(1)
                .execute()
                .getFirstRow();
        var id = row.getLong("test_a_id");
        var name = row.getString("test_a_name");
        getLogger().info("existed row", row);
        var afx = new UpdateStatement()
                .table("test_a")
                .setWithValue("test_a_name", name + " -> " + config().getInteger("index"))
                .where(conditionsComponent -> conditionsComponent
                        .comparison(compareCondition -> compareCondition
                                .filedEqualsValue("test_a_id", id))
                        .comparison(compareCondition -> compareCondition
                                .filedEqualsValue("test_a_name", name)))
                .executeForAffectedRows();
        getLogger().info("modified afx " + afx);
        return afx;
    }

}
