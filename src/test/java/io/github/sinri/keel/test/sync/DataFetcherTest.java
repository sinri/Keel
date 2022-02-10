package io.github.sinri.keel.test.sync;

import io.github.sinri.keel.Keel;
import io.github.sinri.keel.core.DuplexExecutor;
import io.github.sinri.keel.mysql.MySQLExecutor;
import io.github.sinri.keel.test.SharedTestBootstrap;
import io.vertx.core.Future;

import java.sql.SQLException;
import java.sql.Statement;

public class DataFetcherTest {
    public static void main(String[] args) {
        SharedTestBootstrap.initialize();

//        test1();
        test2();
    }

    private static void test1() {
        DuplexExecutor<String> stringDuoFetcherWrapper = new DuplexExecutor<>();
        stringDuoFetcherWrapper
                .setAsyncExecutor(v -> Future.succeededFuture("async"))
                .setSyncExecutor(stringSyncExecuteResult -> {
                    try {
                        //stringSyncExecuteResult.setResult("sync");
                        throw new Exception("sync error");
                    } catch (Exception e) {
                        stringSyncExecuteResult.setError(e);
                    }
                    return stringSyncExecuteResult;
                });

        try {
            Keel.outputLogger("").info(stringDuoFetcherWrapper.executeSync());
        } catch (Exception e) {
            Keel.outputLogger("").error(e.getMessage());
        }

        stringDuoFetcherWrapper.executeAsync()
                .compose(s -> {
                    Keel.outputLogger("").info(s);
                    return Future.succeededFuture();
                })
                .eventually(v -> {
                    Keel.getVertx().close();
                    return Future.succeededFuture();
                });
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
}
