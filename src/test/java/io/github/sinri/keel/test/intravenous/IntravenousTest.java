package io.github.sinri.keel.test.intravenous;

import io.github.sinri.keel.Keel;
import io.github.sinri.keel.mysql.exception.KeelSQLResultRowIndexError;
import io.github.sinri.keel.mysql.matrix.ResultMatrixWithVertx;
import io.github.sinri.keel.servant.intravenous.KeelIntravenous;
import io.github.sinri.keel.servant.intravenous.KeelIntravenousConsumer;
import io.github.sinri.keel.servant.intravenous.KeelIntravenousDrop;
import io.github.sinri.keel.servant.intravenous.KeelIntravenousTaskConclusion;
import io.github.sinri.keel.test.SharedTestBootstrap;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;

public class IntravenousTest {
    public static void main(String[] args) {
        SharedTestBootstrap.initialize();

        Consumer consumer = new Consumer();
        KeelIntravenous<JsonObject, Drop> intravenous = new KeelIntravenous<>(consumer);
        intravenous.deployMe();

        for (var i = 0; i < 10; i++) {
            int finalI = i;
            Future.succeededFuture()
                    .compose(v -> {
                        intravenous.drip(new Drop("T-" + finalI));
                        return Future.succeededFuture();
                    });
        }
    }

    private static class Consumer implements KeelIntravenousConsumer<JsonObject, Drop> {

        @Override
        public Future<KeelIntravenousTaskConclusion<JsonObject>> handle(Drop drop) {
            return Keel.getMySQLKit().withConnection(sqlConnection -> {
                return sqlConnection.query("select now() as x")
                        .execute()
                        .compose(rows -> {
                            return Future.succeededFuture(new ResultMatrixWithVertx(rows));
                        })
                        .compose(resultMatrixWithVertx -> {
                            try {
                                var x = resultMatrixWithVertx.getOneColumnOfFirstRowAsDateTime("x");

                                return Future.succeededFuture(
                                        new TaskConclusion(
                                                drop.getReference(),
                                                true,
                                                x,
                                                resultMatrixWithVertx.getFirstRow()
                                        )
                                );
                            } catch (KeelSQLResultRowIndexError e) {
//                                e.printStackTrace();
                                return Future.succeededFuture(
                                        new TaskConclusion(
                                                drop.getReference(),
                                                false,
                                                e.getClass() + ": " + e.getMessage()
                                        )
                                );
                            }
                        });
            });
        }
    }

    private static class Drop implements KeelIntravenousDrop {

        private final String reference;

        public Drop(String reference) {
            this.reference = reference;
        }

        @Override
        public String getReference() {
            return this.reference;
        }
    }

    private static class TaskConclusion implements KeelIntravenousTaskConclusion<JsonObject> {

        private final String taskReference;
        private final boolean done;
        private final String feedback;
        private final JsonObject result;

        public TaskConclusion(String taskReference, boolean done) {
            this.taskReference = taskReference;
            this.done = done;
            this.feedback = "";
            this.result = null;
        }

        public TaskConclusion(String taskReference, boolean done, String feedback) {
            this.taskReference = taskReference;
            this.done = done;
            this.feedback = feedback;
            this.result = null;
        }

        public TaskConclusion(String taskReference, boolean done, String feedback, JsonObject result) {
            this.taskReference = taskReference;
            this.done = done;
            this.feedback = feedback;
            this.result = result;
        }

        @Override
        public String getTaskReference() {
            return null;
        }

        @Override
        public boolean isDone() {
            return this.done;
        }

        @Override
        public String getFeedback() {
            return this.feedback;
        }

        @Override
        public JsonObject getResult() {
            return this.result;
        }
    }

}
