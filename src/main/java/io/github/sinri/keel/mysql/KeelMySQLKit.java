package io.github.sinri.keel.mysql;

import io.github.sinri.keel.mysql.matrix.ResultMatrix;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.mysqlclient.MySQLClient;
import io.vertx.mysqlclient.MySQLPool;
import io.vertx.sqlclient.SqlConnection;
import io.vertx.sqlclient.Transaction;
import io.vertx.sqlclient.Tuple;

import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Function;

public class KeelMySQLKit {
    private final MySQLPool pool;

    public KeelMySQLKit(Vertx vertx, KeelMySQLConfig configForMySQL) {
        System.out.println("useAffectedRows: " + configForMySQL.useAffectedRows);
        pool = MySQLPool.pool(
                vertx,
                configForMySQL.buildMySQLConnectOptions(),
                configForMySQL.buildPoolOptions()
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

    public MySQLPool getPool() {
        return pool;
    }

    /**
     * @param sqlConnection
     * @param sqlTemplate
     * @param batch
     * @param useRecover
     * @return
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
     * @param sqlConnection
     * @param sqlTemplate
     * @param data
     * @param useRecover
     * @return
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
     * @param sqlConnection
     * @param sqlTemplate
     * @param data
     * @param useRecover
     * @return
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
     * @param sqlConnection
     * @param sqlTemplate
     * @param useRecover
     * @return
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
     * @param sqlConnection
     * @param sqlTemplate
     * @param data
     * @param useRecover
     * @return
     * @since 1.1
     */
    public static Future<ResultMatrix> executeSqlForResultMatrix(
            SqlConnection sqlConnection,
            String sqlTemplate,
            Tuple data,
            boolean useRecover
    ) {
        Future<ResultMatrix> future = sqlConnection.preparedQuery(sqlTemplate).execute(data).compose(rows -> {
            ResultMatrix resultMatrix = new ResultMatrix(rows);
            return Future.succeededFuture(resultMatrix);
        });
        if (useRecover) {
            future = future.recover(throwable -> Future.succeededFuture(null));
        }
        return future;
    }

    /**
     * @param sqlConnection
     * @param sqlTemplate
     * @param useRecover
     * @return
     * @since 1.1
     */
    public static Future<ResultMatrix> executeSqlForResultMatrix(
            SqlConnection sqlConnection,
            String sqlTemplate,
            boolean useRecover
    ) {
        Future<ResultMatrix> future = sqlConnection.preparedQuery(sqlTemplate).execute().compose(rows -> {
            ResultMatrix resultMatrix = new ResultMatrix(rows);
            return Future.succeededFuture(resultMatrix);
        });
        if (useRecover) {
            future = future.recover(throwable -> Future.succeededFuture(null));
        }
        return future;
    }

    @Deprecated
    public void executeInTransaction_V1(
            Function<SqlConnection, Future<Object>> transactionBody,
            Function<Object, Void> doneFunction,
            Function<Throwable, Void> errorFunction
    ) {
        pool.getConnection(sqlConnectionAsyncResult -> {
            if (sqlConnectionAsyncResult.failed()) {
                errorFunction.apply(sqlConnectionAsyncResult.cause());
                return;
            }
            SqlConnection sqlConnection = sqlConnectionAsyncResult.result();
            sqlConnection.begin(transactionAsyncResult -> {
                if (transactionAsyncResult.failed()) {
                    errorFunction.apply(sqlConnectionAsyncResult.cause());
                    return;
                }

                Transaction transaction = transactionAsyncResult.result();

                Future<Object> transactionResultFuture = transactionBody.apply(sqlConnection);
                transactionResultFuture.onSuccess(result -> transaction.commit(commitAsyncResult -> {
                    if (commitAsyncResult.failed()) {
                        errorFunction.apply(commitAsyncResult.cause());
                    } else {
                        doneFunction.apply(result);
                    }
                    // after result confirmed committing, or error, connection should be closed.
                    sqlConnection.close();
                })).onFailure(throwable -> transaction.rollback(rollbackAsyncResult -> {
                    errorFunction.apply(throwable);
                    // after rollback, connection should be closed.
                    sqlConnection.close();
                }));
            });
        });
    }

    public void executeInTransaction(
            Function<SqlConnection, Future<Object>> transactionBody,
            Function<Object, Void> doneFunction,
            Function<Throwable, Void> errorFunction
    ) {
        // rewrite since 1.1
        getPool().withTransaction(transactionBody)
                .onSuccess(doneFunction::apply)
                .onFailure(errorFunction::apply);
    }

    public Future<Object> executeInConnection(Function<SqlConnection, Future<Object>> queryFunction) {
        return getPool().withConnection(queryFunction);
    }
}
