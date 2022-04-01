package io.github.sinri.keel.test.verticles.worker;

import io.github.sinri.keel.mysql.statement.SelectStatement;
import io.github.sinri.keel.mysql.statement.UpdateStatement;
import io.github.sinri.keel.verticles.KeelWorkerVerticleWithJDBCForWeb;
import io.vertx.ext.web.RoutingContext;

public class WorkerVerticleD extends KeelWorkerVerticleWithJDBCForWeb<Integer> {
    public WorkerVerticleD(RoutingContext routingContext) {
        super(routingContext);
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

        Thread.sleep(10000L);

        var next = routingContext.queryParam("next").get(0);

        var afx = new UpdateStatement()
                .table("test_a")
                .setWithValue("test_a_name", name + " -> " + next)
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
