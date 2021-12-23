package io.github.sinri.keel.test.web.controller;

import io.github.sinri.keel.Keel;
import io.github.sinri.keel.mysql.KeelMySQLKit;
import io.github.sinri.keel.web.KeelWebRequestController;
import io.vertx.core.Future;
import io.vertx.ext.web.RoutingContext;
import io.vertx.mysqlclient.MySQLClient;
import io.vertx.sqlclient.Tuple;

import java.util.ArrayList;
import java.util.List;

public class MySQLController extends KeelWebRequestController {
    private final KeelMySQLKit mySQLKit;

    public MySQLController(RoutingContext ctx) {
        super(ctx);
        mySQLKit = Keel.getMySQLKit("local");
    }

    public void testA() {
        mySQLKit.executeInTransaction(
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
        mySQLKit.executeInTransaction(
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
        mySQLKit.executeInTransaction(
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
}
