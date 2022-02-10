package io.github.sinri.keel.test.web.controller;

import io.github.sinri.keel.Keel;
import io.github.sinri.keel.mysql.KeelMySQLKit;
import io.github.sinri.keel.mysql.condition.CompareCondition;
import io.github.sinri.keel.mysql.statement.SelectStatement;
import io.github.sinri.keel.mysql.statement.WriteIntoStatement;
import io.github.sinri.keel.web.KeelWebRequestController;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import io.vertx.mysqlclient.MySQLClient;
import io.vertx.sqlclient.SqlConnection;
import io.vertx.sqlclient.Tuple;
import io.vertx.sqlclient.data.Numeric;

import java.util.ArrayList;
import java.util.List;

public class MySQLController extends KeelWebRequestController {
    private final KeelMySQLKit mySQLKit;

    public MySQLController(RoutingContext ctx) {
        super(ctx);
        mySQLKit = Keel.getMySQLKit("local");
    }

    public void testA() {
        mySQLKit.executeInTransaction_V2(
                sqlConnection -> {
                    List<Tuple> batch = new ArrayList<>();
                    batch.add(Tuple.of("Dog1", 2.1, "NORMAL", "2021-12-01 12:04:21"));
                    batch.add(Tuple.of("Dog2", 2.2, "NORMAL", "2021-12-02 14:14:01"));
                    return sqlConnection.preparedQuery("insert into java_test_for_sinri(name,value,status,record_time) values(?,?,?,?)")
                            .executeBatch(batch)
                            .compose(rows -> {
                                long lastInsertId = rows.property(MySQLClient.LAST_INSERTED_ID);
                                // here the lastInsertId would be the first id batch inserted.
                                // if not inserted, would not come here but as failed
                                return Future.succeededFuture(lastInsertId);
                            })
                            .recover(throwable -> {
                                long x = -1;
                                return Future.succeededFuture(x);
                            })
                            .compose(Future::succeededFuture);
                },
                lastInsertId -> {
                    // committed
                    sayOK(lastInsertId);
                    return null;
                },
                throwable -> {
                    sayFail(throwable.getMessage());
                    return null;
                }
        );
    }

    public void testB() {
        mySQLKit.executeInTransaction_V2(
                sqlConnection -> {
                    List<Tuple> batch = new ArrayList<>();
                    batch.add(Tuple.of("Dog1", 2.1, "NORMAL", "2021-12-01 12:04:21"));
                    batch.add(Tuple.of("Dog2", 2.2, "NORMAL", "2021-12-02 14:14:01"));

                    return KeelMySQLKit.executeSqlForLastInsertedID(
                            sqlConnection,
                            "insert into java_test_for_sinri(name,value,status,record_time) values(?,?,?,?)",
                            batch,
                            readParamForTheFirst("useRecover", "no").equals("yes")
                    ).compose(Future::succeededFuture);

//                    return sqlConnection.preparedQuery("insert into java_test_for_sinri(name,value,status,record_time) values(?,?,?,?)")
//                            .executeBatch(batch)
//                            .compose(rows -> {
//                                long lastInsertId = rows.property(MySQLClient.LAST_INSERTED_ID);
//                                // here the lastInsertId would be the first id batch inserted.
//                                // if not inserted, would not come here but as failed
//                                return Future.succeededFuture(lastInsertId);
//                            })
//                            .recover(throwable -> {
//                                long x=-1;
//                                return Future.succeededFuture(x);
//                            })
//                            .compose(Future::succeededFuture);
                },
                lastInsertId -> {
                    // committed
                    sayOK(lastInsertId);
                    return null;
                },
                throwable -> {
                    sayFail(throwable.getMessage());
                    return null;
                }
        );
    }

    public void testC() {
        mySQLKit.executeInTransaction_V2(
                sqlConnection -> {
                    String name = readParamForTheFirst("name");
                    Tuple data = Tuple.of(4, name);
                    return KeelMySQLKit.executeSqlForAffectedRowCount(
                            sqlConnection,
                            "update java_test_for_sinri set value=? where name like concat(?,'%')",
                            data,
                            readParamForTheFirst("useRecover", "no").equals("yes")
                    ).compose(Future::succeededFuture);
                },
                afx -> {
                    // committed
                    sayOK(afx);
                    return null;
                },
                throwable -> {
                    sayFail(throwable.getMessage());
                    return null;
                }
        );
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
                    return Future.succeededFuture(resultMatrix.getOneColumnOfFirstRowAsString("name"));
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
                    return Future.succeededFuture(resultMatrix.getOneColumnOfFirstRowAsNumeric("value"));
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
