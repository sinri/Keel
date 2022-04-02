package io.github.sinri.keel.test.v1.web.controller;

import io.github.sinri.keel.Keel;
import io.github.sinri.keel.mysql.KeelMySQLKit;
import io.github.sinri.keel.mysql.condition.CompareCondition;
import io.github.sinri.keel.mysql.exception.KeelSQLResultRowIndexError;
import io.github.sinri.keel.mysql.statement.SelectStatement;
import io.github.sinri.keel.mysql.statement.WriteIntoStatement;
import io.github.sinri.keel.web.KeelWebRequestController;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import io.vertx.sqlclient.SqlConnection;
import io.vertx.sqlclient.data.Numeric;

public class MySQLController extends KeelWebRequestController {
    private final KeelMySQLKit mySQLKit;

    public MySQLController(RoutingContext ctx) {
        super(ctx);
        mySQLKit = Keel.getMySQLKit("local");
    }

    public Future<JsonObject> testD() {
        return mySQLKit.executeInTransaction(sqlConnection -> {
                    JsonObject row = new JsonObject();
                    return testD1(sqlConnection)
                            .compose(name -> {
                                String newName = name;
                                row.put("name", newName);
                                return testD2(sqlConnection, name);
                            })
                            .compose(numeric -> {
                                row.put("value", numeric.doubleValue());
                                return testD3(sqlConnection, row);
                            });
                })
                .compose(newRecordId -> Future.succeededFuture(new JsonObject().put("newRecordId", newRecordId)));
    }

    private Future<String> testD1(SqlConnection sqlConnection) {
        SelectStatement selectStatement = new SelectStatement()
                .column(columnComponent -> columnComponent.field("name"))
                .from("java_test_for_sinri")
                .limit(1);
        System.out.println("testD1 sql " + selectStatement.toString());
        return selectStatement.execute(sqlConnection)
                .compose(resultMatrix -> {
                    try {
                        return Future.succeededFuture(resultMatrix.getOneColumnOfFirstRowAsString("name"));
                    } catch (KeelSQLResultRowIndexError e) {
                        return Future.failedFuture(e);
                    }
                });
    }

    private Future<Numeric> testD2(SqlConnection sqlConnection, String name) {
        SelectStatement selectStatement = new SelectStatement()
                .column(columnComponent -> columnComponent.field("value"))
                .from("java_test_for_sinri")
                .where(conditionsComponent -> conditionsComponent
                        .comparison(compareCondition -> compareCondition
                                .compare("name").operator(CompareCondition.OP_EQ).againstValue(name)
                        )
                )
                .limit(1);
        System.out.println("testD2 sql " + selectStatement.toString());
        return selectStatement.execute(sqlConnection)
                .compose(resultMatrix -> {
                    try {
                        return Future.succeededFuture(resultMatrix.getOneColumnOfFirstRowAsNumeric("value"));
                    } catch (KeelSQLResultRowIndexError e) {
                        return Future.failedFuture(e);
                    }
                });
    }

    private Future<Long> testD3(SqlConnection sqlConnection, JsonObject row) {
        WriteIntoStatement writeIntoStatement = new WriteIntoStatement()
                .intoTable("java_test_for_sinri")
                .macroWriteOneRowWithJsonObject(row
                        .put("status", "COPIED")
                        .put("record_time", KeelMySQLKit.nowAsMySQLDatetime())
                );
        System.out.println("testD3 sql " + writeIntoStatement.toString());
        return writeIntoStatement.executeForLastInsertedID(sqlConnection);
    }
}
