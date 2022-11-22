package io.github.sinri.keel.test.mysql;

import io.github.sinri.keel.Keel;
import io.github.sinri.keel.core.logger.KeelLogger;
import io.github.sinri.keel.mysql.matrix.TableRowClassGenerator;
import io.github.sinri.keel.test.SharedTestBootstrap;

public class TableRowGenerateTest {
    public static void main(String[] args) {
        SharedTestBootstrap.initialize(v0 -> {
            KeelLogger logger = Keel.outputLogger("main");

            SharedTestBootstrap.getMySQLKit()
                    .withConnection(sqlConnection -> {
                        return new TableRowClassGenerator(sqlConnection)
//                            .forSchema("docker_test")
                                .setRewrite(true)
                                .setSupportLooseEnum(true)
                                .generate(
                                        "io.github.sinri.keel.test.mysql.ag",
                                        "/Users/leqee/code/Keel/src/test/java/io/github/sinri/keel/test/mysql/ag"
                                );
                    })
                    .onSuccess(v -> {
                        logger.info("classes generated");
                    })
                    .onFailure(throwable -> {
                        logger.exception(throwable);
                    })
                    .eventually(v -> {
                        return Keel.getVertx().close();
                    });
        });


    }

//    public static Future<JavaTestForSinriTableRow> queryWithTestRecordId(SqlConnection sqlConnection, long test_record_id) {
//        return new SelectStatement()
//                .from(JavaTestForSinriTableRow.TABLE)
//                .where(conditionsComponent -> conditionsComponent.quickMapping("test_record_id", test_record_id))
//                .limit(1)
//                .queryForOneRow(sqlConnection, JavaTestForSinriTableRow.class);
//    }
}

