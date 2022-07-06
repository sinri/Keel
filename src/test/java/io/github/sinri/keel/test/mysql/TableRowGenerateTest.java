package io.github.sinri.keel.test.mysql;

import io.github.sinri.keel.Keel;
import io.github.sinri.keel.core.logger.KeelLogger;
import io.github.sinri.keel.mysql.matrix.TableRowClassGenerator;
import io.github.sinri.keel.mysql.statement.SelectStatement;
import io.github.sinri.keel.test.SharedTestBootstrap;
import io.github.sinri.keel.test.mysql.ag.JavaTestForSinriTableRow;
import io.vertx.core.Future;
import io.vertx.sqlclient.SqlConnection;

public class TableRowGenerateTest {
    public static void main(String[] args) {
        SharedTestBootstrap.initialize();

        KeelLogger logger = Keel.outputLogger("main");

        SharedTestBootstrap.getMySQLKit().getPool().withConnection(sqlConnection -> {
                    return new TableRowClassGenerator(sqlConnection)
//                            .forSchema("docker_test")
                            .generate(
                                    "io.github.sinri.keel.test.mysql.ag",
                                    "/Users/leqee/code/Keel/src/test/java/io/github/sinri/keel/test/mysql/ag"
                            );
                })
                .onSuccess(v -> {
                    logger.info("classes generated");
                })
                .compose(v -> {
                    return SharedTestBootstrap.getMySQLKit().getPool().withConnection(sqlConnection -> {
                        return queryWithTestRecordId(sqlConnection, 1)
                                .compose(javaTestForSinriTableRow -> {
                                    String fDate = javaTestForSinriTableRow.getFDate();
                                    String fTime = javaTestForSinriTableRow.getFTime();
                                    String fTimestamp = javaTestForSinriTableRow.getFTimestamp();

                                    logger.info("date: " + fDate + " as " + fDate.getClass());
                                    logger.info("time: " + fTime + " as " + fTime.getClass());
                                    logger.info("timestamp: " + fTimestamp + " as " + fTimestamp.getClass());
                                    return Future.succeededFuture();
                                });
                    });
                })
                .onFailure(throwable -> {
                    logger.exception(throwable);
                })
                .eventually(v -> {
                    return Keel.getVertx().close();
                });
    }

    public static Future<JavaTestForSinriTableRow> queryWithTestRecordId(SqlConnection sqlConnection, long test_record_id) {
        return new SelectStatement()
                .from(JavaTestForSinriTableRow.TABLE)
                .where(conditionsComponent -> conditionsComponent.quickMapping("test_record_id", test_record_id))
                .limit(1)
                .queryForOneRow(sqlConnection, JavaTestForSinriTableRow.class);
    }
}

