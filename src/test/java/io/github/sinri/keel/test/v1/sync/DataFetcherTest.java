package io.github.sinri.keel.test.v1.sync;

import io.github.sinri.keel.Keel;
import io.github.sinri.keel.mysql.DuplexExecutorForMySQL;
import io.github.sinri.keel.mysql.MySQLExecutor;
import io.github.sinri.keel.mysql.exception.KeelSQLResultRowIndexError;
import io.github.sinri.keel.mysql.matrix.ResultMatrixWithJDBC;
import io.github.sinri.keel.mysql.matrix.ResultMatrixWithVertx;
import io.github.sinri.keel.test.SharedTestBootstrap;
import io.vertx.core.Future;

import java.sql.SQLException;
import java.sql.Statement;

public class DataFetcherTest {
    public static void main(String[] args) {
        SharedTestBootstrap.initialize();

//        test1();
//        test2();
        test3();
    }

    private static void test2() {
//        DuplexExecutorForMySQL<String> duplexExecutorForMySQL = DuplexExecutorForMySQL.build(
//                sqlConnection -> Future.succeededFuture("async"),
//                statement -> "sync"
//        );

        MySQLExecutor<String> duplexExecutorForMySQL = MySQLExecutor.build(
                sqlConnection -> Future.failedFuture("async error"),
                statement -> {
                    throw new SQLException("sync error");
                }
        );

        SharedTestBootstrap.getMySQLKit().executeInTransaction(duplexExecutorForMySQL::executeAsync)
                .onSuccess(x -> {
                    Keel.outputLogger("").info(x);
                })
                .onFailure(throwable -> {
                    Keel.outputLogger("").exception(throwable);
                })
                .eventually(v -> {
                    Statement statement = SharedTestBootstrap.getMySqlJDBC().getThreadLocalStatementWrapper().getCurrentThreadLocalStatement();
                    String s = null;
                    try {
                        s = duplexExecutorForMySQL.executeSync(statement);
                        Keel.outputLogger("").info(s);
                    } catch (SQLException e) {
                        Keel.outputLogger("").exception(e);
                    }
                    return Future.succeededFuture();
                })
                .eventually(v -> {
                    return Keel.getVertx().close();
                });

    }

    private static void test3() {
        var sql = "select 'ooo' as t";
        DuplexExecutorForMySQL<String> stringMySQLExecutorV2 = new DuplexExecutorForMySQL<String>(
                sqlConnection -> {
                    return sqlConnection.query(sql).execute()
                            .compose(rows -> {
                                return Future.succeededFuture(new ResultMatrixWithVertx(rows));
                            })
                            .compose(resultMatrixWithVertx -> {
                                try {
                                    return Future.succeededFuture(resultMatrixWithVertx.getFirstRow().getString("t"));
                                } catch (KeelSQLResultRowIndexError e) {
                                    return Future.failedFuture(e);
                                }
                            });
                },
                statement -> {
                    try {
                        return new ResultMatrixWithJDBC(statement.executeQuery(sql))
                                .getFirstRow()
                                .getString("t");
                    } catch (KeelSQLResultRowIndexError e) {
                        return null;
                    }
                }
        );

        Statement currentThreadLocalStatement = SharedTestBootstrap.getMySqlJDBC().getThreadLocalStatementWrapper().getCurrentThreadLocalStatement();
        try {
            String x = stringMySQLExecutorV2.executeSync(currentThreadLocalStatement);
            System.out.println(x);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        SharedTestBootstrap.getMySQLKit().getPool().withConnection(sqlConnection -> {
                    return stringMySQLExecutorV2.executeAsync(sqlConnection);
                })
                .compose(x -> {
                    System.out.println(x);
                    return Future.succeededFuture();
                })
                .eventually(v -> {
                    return Keel.getVertx().close();
                });
    }
}
