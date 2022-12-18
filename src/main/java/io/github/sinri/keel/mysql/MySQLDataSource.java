package io.github.sinri.keel.mysql;

import io.github.sinri.keel.facade.Keel;
import io.github.sinri.keel.mysql.exception.KeelMySQLException;
import io.github.sinri.keel.mysql.matrix.ResultMatrix;
import io.github.sinri.keel.mysql.statement.SelectStatement;
import io.vertx.core.Future;
import io.vertx.mysqlclient.MySQLClient;
import io.vertx.mysqlclient.MySQLPool;
import io.vertx.sqlclient.SqlConnection;
import io.vertx.sqlclient.TransactionRollbackException;
import io.vertx.sqlclient.Tuple;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

public class MySQLDataSource {
    private final Keel keel;
    private final MySQLPool pool;
    private final KeelMySQLConfigure configure;

    public MySQLDataSource(Keel keel, KeelMySQLConfigure configure) {
        this.keel = keel;
        this.configure = configure;
        pool = MySQLPool.pool(
                keel.getVertx(),
                configure.getConnectOptions(),
                configure.getPoolOptions()
        );
    }

    protected static String makePlaceholderString(int x) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < x; i++) {
            if (i > 0) result.append(",");
            result.append("?");
        }
        return result.toString();
    }

    protected static String makeStandardWidthField(int x, int w) {
        StringBuilder s = new StringBuilder("" + x);
        if (s.length() < w) {
            for (int i = 0; i < w - s.length(); i++) {
                s.insert(0, "0");
            }
        }
        return String.valueOf(s);
    }

    public static String toMySQLDatetime(LocalDateTime datetime) {
        return makeStandardWidthField(datetime.getYear(), 4)
                + "-" + makeStandardWidthField(datetime.getMonthValue(), 2)
                + "-" + makeStandardWidthField(datetime.getDayOfMonth(), 2)
                + " "
                + makeStandardWidthField(datetime.getHour(), 2)
                + ":" + makeStandardWidthField(datetime.getMinute(), 2)
                + ":" + makeStandardWidthField(datetime.getSecond(), 2);
    }

    /**
     * @return Y-m-d H:i:s
     * @since 1.7
     */
    public static String nowAsMySQLDatetime() {
        return toMySQLDatetime(LocalDateTime.now());
    }

    /**
     * @since 1.1
     */
    public static Future<Long> executeSqlForLastInsertedID(
            SqlConnection sqlConnection,
            String sqlTemplate,
            List<Tuple> batch,
            boolean useRecover
    ) {
        Future<Long> future = sqlConnection.preparedQuery(sqlTemplate).executeBatch(batch).compose(rows -> {
            long lastInsertId = rows.property(MySQLClient.LAST_INSERTED_ID);
            // here the lastInsertId would be the first id batch inserted.
            // if not inserted, would not come here but as failed
            return Future.succeededFuture(lastInsertId);
        });
        if (useRecover) {
            future = future.recover(throwable -> Future.succeededFuture(-1L));
        }
        return future;
    }

    /**
     * @since 1.1
     */
    public static Future<Long> executeSqlForLastInsertedID(
            SqlConnection sqlConnection,
            String sqlTemplate,
            Tuple data,
            boolean useRecover
    ) {
        Future<Long> future = sqlConnection.preparedQuery(sqlTemplate).execute(data).compose(rows -> {
            long lastInsertId = rows.property(MySQLClient.LAST_INSERTED_ID);
            // here the lastInsertId would be the first id batch inserted.
            // if not inserted, would not come here but as failed
            return Future.succeededFuture(lastInsertId);
        });
        if (useRecover) {
            future = future.recover(throwable -> Future.succeededFuture(-1L));
        }
        return future;
    }

    /**
     * @since 1.1
     */
    public static Future<Integer> executeSqlForAffectedRowCount(
            SqlConnection sqlConnection,
            String sqlTemplate,
            Tuple data,
            boolean useRecover
    ) {
        Future<Integer> future = sqlConnection.preparedQuery(sqlTemplate).execute(data).compose(rows -> {
            // conditions matched zero rows: afx = 0
            // conditions matched rows and modified them all: afx = all rows
            // if `useAffectedRows` is set to `true`,
            //   conditions matched rows but not modified any: afx = 0
            //   conditions matched rows but modified partly: afx = exact partly rows
            // otherwise,
            //   conditions matched rows but not modified any: afx = all rows
            //   conditions matched rows but modified partly: afx = all rows
            int afx = rows.rowCount();
            return Future.succeededFuture(afx);
        });
        if (useRecover) {
            future = future.recover(throwable -> Future.succeededFuture(-1));
        }
        return future;
    }

    /**
     * @since 1.1
     */
    public static Future<Integer> executeSqlForAffectedRowCount(
            SqlConnection sqlConnection,
            String sqlTemplate,
            boolean useRecover
    ) {
        Future<Integer> future = sqlConnection.preparedQuery(sqlTemplate).execute().compose(rows -> {
            // conditions matched zero rows: afx = 0
            // conditions matched rows and modified them all: afx = all rows
            // if `useAffectedRows` is set to `true`,
            //   conditions matched rows but not modified any: afx = 0
            //   conditions matched rows but modified partly: afx = exact partly rows
            // otherwise,
            //   conditions matched rows but not modified any: afx = all rows
            //   conditions matched rows but modified partly: afx = all rows
            int afx = rows.rowCount();
            return Future.succeededFuture(afx);
        });
        if (useRecover) {
            future = future.recover(throwable -> Future.succeededFuture(-1));
        }
        return future;
    }

    /**
     * @since 1.1
     */
    public static Future<ResultMatrix> executeSqlForResultMatrix(
            SqlConnection sqlConnection,
            String sqlTemplate,
            Tuple data,
            boolean useRecover
    ) {
        Future<ResultMatrix> future = sqlConnection.preparedQuery(sqlTemplate).execute(data).compose(rows -> {
            return Future.succeededFuture(ResultMatrix.create(rows));
        });
        if (useRecover) {
            future = future.recover(throwable -> Future.succeededFuture(null));
        }
        return future;
    }

    /**
     * @since 1.1
     */
    public static Future<ResultMatrix> executeSqlForResultMatrix(
            SqlConnection sqlConnection,
            String sqlTemplate,
            boolean useRecover
    ) {
        Future<ResultMatrix> future = sqlConnection.preparedQuery(sqlTemplate).execute().compose(rows -> {
            return Future.succeededFuture(ResultMatrix.create(rows));
        });
        if (useRecover) {
            future = future.recover(throwable -> Future.succeededFuture(null));
        }
        return future;
    }

    public KeelMySQLConfigure getConfigure() {
        return configure;
    }

    /**
     * @since 2.8
     */
    public <T> Future<T> withConnection(Function<SqlConnection, Future<T>> function) {
        return pool.getConnection()
                .recover(throwable -> Future.failedFuture(new KeelMySQLException(
                        "When executing `io.github.sinri.keel.mysql.KeelMySQLKit.withConnection`," +
                                " failed to get connection from the pool, cause message: " +
                                throwable.getMessage(),
                        throwable
                )))
                .compose(sqlConnection -> Future.succeededFuture()
                        .compose(v -> {
                            try {
                                return function.apply(sqlConnection);
                            } catch (Throwable throwable) {
                                return Future.failedFuture(throwable);
                            }
                        })
                        .eventually(v -> sqlConnection.close())
                );
    }

    /**
     * @since 2.8
     */
    public <T> Future<T> withTransaction(Function<SqlConnection, Future<T>> function) {
        return pool.getConnection()
                .recover(throwable -> Future.failedFuture(new KeelMySQLException(
                        "When executing `io.github.sinri.keel.mysql.KeelMySQLKit.withTransaction`," +
                                " failed to get connection from the pool, cause message: " +
                                throwable.getMessage(),
                        throwable
                )))
                .compose(sqlConnection -> sqlConnection.begin()
                        .compose(transaction -> Future.succeededFuture()
                                .compose(v -> {
                                    try {
                                        return function.apply(sqlConnection);
                                    } catch (Throwable throwable) {
                                        return Future.failedFuture(throwable);
                                    }
                                })
                                .compose(res -> transaction.commit()
                                        .compose((v) -> Future.succeededFuture(res))
                                )
                                .recover(err -> {
                                            if (err instanceof TransactionRollbackException) {
                                                return Future.failedFuture(err);
                                            }
                                            return transaction.rollback()
                                                    .compose(v -> Future.failedFuture(new KeelMySQLException(
                                                                    "When executing `io.github.sinri.keel.mysql.KeelMySQLKit.withTransaction`," +
                                                                            " a rollback performed after an error met, cause message: " +
                                                                            err.getMessage(),
                                                                    err
                                                            )),
                                                            rollbackFailure -> Future.failedFuture(new KeelMySQLException(
                                                                    "When executing `io.github.sinri.keel.mysql.KeelMySQLKit.withTransaction`," +
                                                                            " after an error met (cause message: " + err.getMessage() + ")," +
                                                                            " a rollback performed but failed (cause message: " + rollbackFailure.getMessage() + ")",
                                                                    new KeelMySQLException(
                                                                            "When executing `io.github.sinri.keel.mysql.KeelMySQLKit.withTransaction`," +
                                                                                    " a rollback should be performed after an error met, cause message: " +
                                                                                    err.getMessage(),
                                                                            err
                                                                    )
                                                            ))
                                                    )
                                                    .compose(v -> {
                                                        // would not come here
                                                        return Future.succeededFuture();
                                                    });

                                        }
                                )
                        )
                        .onComplete(ar -> sqlConnection.close())
                );
    }

    /**
     * @param transactionBody the function with sql connection for future
     * @param <T>             the final result class/type
     * @return future with final result if committed, or failed future if rollback
     * @since 1.10
     */
    public <T> Future<T> executeInTransaction(Function<SqlConnection, Future<T>> transactionBody) {
        AtomicReference<T> finalResult = new AtomicReference<>();
        AtomicReference<Throwable> cause = new AtomicReference<>();
        return pool
                .withTransaction(transactionBody)
                .onSuccess(finalResult::set)
                .onFailure(cause::set)
                .eventually(v -> {
                    if (cause.get() == null) {
                        return Future.succeededFuture(finalResult.get());
                    }
                    return Future.failedFuture(cause.get());
                });
    }

    /**
     * @param selection the SELECT STATEMENT BUILDER
     * @return the future for ResultMatrix, nullable
     * @since 1.4
     */
    public Future<ResultMatrix> queryInConnection(SelectStatement selection) {
        return pool.withConnection(
                sqlConnection -> MySQLDataSource.executeSqlForResultMatrix(
                        sqlConnection,
                        selection.toString(),
                        false
                )
        );
    }
}