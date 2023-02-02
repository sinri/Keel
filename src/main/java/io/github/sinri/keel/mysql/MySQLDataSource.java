package io.github.sinri.keel.mysql;

import io.github.sinri.keel.facade.Keel;
import io.github.sinri.keel.facade.async.KeelAsyncKit;
import io.github.sinri.keel.helper.KeelHelpers;
import io.github.sinri.keel.mysql.matrix.ResultMatrix;
import io.github.sinri.keel.mysql.statement.SelectStatement;
import io.vertx.core.Future;
import io.vertx.mysqlclient.MySQLClient;
import io.vertx.mysqlclient.MySQLPool;
import io.vertx.sqlclient.*;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

public class MySQLDataSource {
    private final MySQLPool pool;
    private final KeelMySQLConfiguration configuration;

    public MySQLDataSource(KeelMySQLConfiguration configuration) {
        this.configuration = configuration;
        pool = MySQLPool.pool(
                Keel.getVertx(),
                configuration.getConnectOptions(),
                configuration.getPoolOptions()
        );
    }

    @Deprecated(since = "3.0.0")
    protected static String makePlaceholderString(int x) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < x; i++) {
            if (i > 0) result.append(",");
            result.append("?");
        }
        return result.toString();
    }

    @Deprecated(since = "3.0.0")
    protected static String makeStandardWidthField(int x, int w) {
        StringBuilder s = new StringBuilder("" + x);
        if (s.length() < w) {
            for (int i = 0; i < w - s.length(); i++) {
                s.insert(0, "0");
            }
        }
        return String.valueOf(s);
    }

    @Deprecated(since = "3.0.0")
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
    @Deprecated(since = "3.0.0")
    public static String nowAsMySQLDatetime() {
        return KeelHelpers.datetimeHelper().getCurrentDateExpression("yyyy-MM-dd HH:mm:ss");
//        return toMySQLDatetime(LocalDateTime.now());
    }

    /**
     * @since 1.1
     */
    @Deprecated(since = "3.0.0")
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
    @Deprecated(since = "3.0.0")
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
    @Deprecated(since = "3.0.0")
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
    @Deprecated(since = "3.0.0")
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
    @Deprecated(since = "3.0.0")
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
    @Deprecated(since = "3.0.0")
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

    public KeelMySQLConfiguration getConfiguration() {
        return configuration;
    }

    /**
     * @since 2.8
     */
    public <T> Future<T> withConnection(Function<SqlConnection, Future<T>> function) {
        return pool.withConnection(function);
    }

    /**
     * @since 2.8
     */
    public <T> Future<T> withTransaction(Function<SqlConnection, Future<T>> function) {
        return pool.withTransaction(function);
    }

    /**
     * @param transactionBody the function with sql connection for future
     * @param <T>             the final result class/type
     * @return future with final result if committed, or failed future if rollback
     * @since 1.10
     */
    @Deprecated(since = "3.0.0")
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
    @Deprecated(since = "3.0.0")
    public Future<ResultMatrix> queryInConnection(SelectStatement selection) {
        return pool.withConnection(
                sqlConnection -> MySQLDataSource.executeSqlForResultMatrix(
                        sqlConnection,
                        selection.toString(),
                        false
                )
        );
    }

    /**
     * @since 3.0.0
     */
    public Future<Void> queryForRowStream(String sql, int streamFetchSize, Function<Row, Future<Void>> rowAsyncHandler) {
        return this.pool.withTransaction(sqlConnection -> {
            return sqlConnection.prepare(sql).compose(preparedStatement -> {
                Cursor cursor = preparedStatement.cursor();
                return KeelAsyncKit.repeatedlyCall(routineResult -> {
                            return cursor.read(streamFetchSize)
                                    .compose(rows -> KeelAsyncKit
                                            .parallelForAllSuccess(rows, rowAsyncHandler))
                                    .eventually(v -> {
                                        if (!cursor.hasMore()) {
                                            routineResult.stop();
                                        }
                                        return Future.succeededFuture();
                                    });
                        })
                        .eventually(eventually -> cursor.close());
            });
        });
    }

    /**
     * @since 3.0.0 EXPERIMENTAL
     */
    public Future<Iterator<Row>> queryForStreamRowIterator(String sql, int streamFetchSize) {
        // todo 确认能不能按期望运行
        return this.pool.withTransaction(sqlConnection -> {
            return sqlConnection.prepare(sql).compose(preparedStatement -> {
                Cursor cursor = preparedStatement.cursor();
                IteratorOverCursor iteratorOverCursor = new IteratorOverCursor(cursor, streamFetchSize);
                return Future.succeededFuture(iteratorOverCursor);
            });
        });
    }

    /**
     * @since 3.0.0 EXPERIMENTAL
     */
    public Future<Iterable<Row>> queryForStreamRowIterable(String sql, int streamFetchSize) {
        // todo 确认能不能按期望运行
        return this.pool.withTransaction(sqlConnection -> {
            return sqlConnection.prepare(sql).compose(preparedStatement -> {
                Cursor cursor = preparedStatement.cursor();
                IterableOverCursor iterableOverCursor = new IterableOverCursor(cursor, streamFetchSize);
                return Future.succeededFuture(iterableOverCursor);
            });
        });
    }

    /**
     * @since 3.0.0
     */
    private static class IteratorOverCursor implements Iterator<Row> {
        private final Cursor cursor;
        private final int batchSize;
        private Iterator<Row> rowIterator;
        private Throwable error;
        private boolean over;
        private boolean loading;

        public IteratorOverCursor(Cursor cursor) {
            this.cursor = cursor;
            this.batchSize = 100;
            load();
        }

        public IteratorOverCursor(Cursor cursor, int batchSize) {
            this.cursor = cursor;
            this.batchSize = batchSize;
            load();
        }

        private void load() {
            over = false;
            error = null;
            loading = true;
            this.cursor.read(batchSize, rowSetAsyncResult -> {
                if (rowSetAsyncResult.failed()) {
                    error = rowSetAsyncResult.cause();
                    rowIterator = null;
                    over = true;
                } else {
                    RowSet<Row> rowSet = rowSetAsyncResult.result();
                    rowIterator = rowSet.iterator();
                    over = !cursor.hasMore();
                }

                loading = false;
            });
        }

        public Throwable getError() {
            return error;
        }

        protected Iterator<Row> getRowIterator() {
            if (loading) {
                try {
                    Thread.sleep(100L);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                return getRowIterator();
            }
            return rowIterator;
        }

        @Override
        public boolean hasNext() {
            if (over) return false;
            Iterator<Row> iterator = getRowIterator();
            if (iterator == null) return false;
            return iterator.hasNext();
        }

        @Override
        public Row next() {
            Iterator<Row> iterator = getRowIterator();
            if (iterator == null) throw new NoSuchElementException();
            return iterator.next();
        }
    }

    /**
     * @since 3.0.0
     */
    private static class IterableOverCursor implements Iterable<Row> {

        private final IteratorOverCursor iteratorOverCursor;

        public IterableOverCursor(Cursor cursor) {
            this.iteratorOverCursor = new IteratorOverCursor(cursor);
        }

        public IterableOverCursor(Cursor cursor, int batchSize) {
            this.iteratorOverCursor = new IteratorOverCursor(cursor, batchSize);
        }

        @NotNull
        @Override
        public Iterator<Row> iterator() {
            return this.iteratorOverCursor;
        }
    }
}
