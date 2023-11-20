package io.github.sinri.keel.mysql;

import io.github.sinri.keel.facade.Keel;
import io.github.sinri.keel.facade.async.KeelAsyncKit;
import io.github.sinri.keel.mysql.exception.KeelMySQLConnectionException;
import io.github.sinri.keel.mysql.exception.KeelMySQLException;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.mysqlclient.MySQLPool;
import io.vertx.sqlclient.*;

import javax.annotation.Nonnull;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

public class MySQLDataSource {
    private final MySQLPool pool;
    private final KeelMySQLConfiguration configuration;
    /**
     * @since 3.0.2
     */
    private final AtomicInteger connectionAvailableCounter = new AtomicInteger(0);

    public MySQLDataSource(KeelMySQLConfiguration configuration) {
        this(configuration, sqlConnection -> Future.succeededFuture());
    }

    /**
     * @since 3.0.2
     */
    public MySQLDataSource(KeelMySQLConfiguration configuration, @Nonnull Function<SqlConnection, Future<Void>> connectionSetUpFunction) {
        this.configuration = configuration;
        pool = MySQLPool.pool(
                Keel.getVertx(),
                configuration.getConnectOptions(),
                configuration.getPoolOptions()
        );
        pool.connectHandler(sqlConnection -> {
            connectionSetUpFunction.apply(sqlConnection)
                    .onComplete(ar -> {
                        connectionAvailableCounter.incrementAndGet();
                        sqlConnection.close();
                    });
        });
    }

    public KeelMySQLConfiguration getConfiguration() {
        return configuration;
    }

    /**
     * @return the number of connections in use right now
     * @since 3.0.2
     */
    public int getAvailableConnectionCount() {
        return connectionAvailableCounter.get();
    }

    /**
     * @since 2.8
     * @since 3.0.2 re-wrap by Keel
     */
    public <T> Future<T> withConnection(Function<SqlConnection, Future<T>> function) {
        // since 3.0.2
        Promise<T> promise = Promise.promise();
        pool.getConnection(ar -> {
            if (ar.failed()) {
                promise.fail(new KeelMySQLConnectionException(
                        "MySQLDataSource Failed to get SqlConnection From Pool " +
                                "`" + this.getConfiguration().getDataSourceName() + "` " +
                                "(available: " + connectionAvailableCounter.get() + "): " +
                                ar.cause(),
                        ar.cause()
                ));
            } else {
                connectionAvailableCounter.decrementAndGet();

                var sqlConnection = ar.result();
                Future.succeededFuture()
                        .compose(v -> function.apply(sqlConnection))
                        .onComplete(tAsyncResult -> {
                            sqlConnection.close();

                            connectionAvailableCounter.incrementAndGet();

                            if (tAsyncResult.failed()) {
                                promise.fail(new KeelMySQLException(
                                        "MySQLDataSource Failed Within SqlConnection: " + tAsyncResult.cause(),
                                        tAsyncResult.cause()
                                ));
                            } else {
                                promise.complete(tAsyncResult.result());
                            }
                        });
            }
        });
        return promise.future();
        //return pool.withConnection(function);
    }

    /**
     * @since 2.8
     * @since 3.0.2 re-wrap by Keel
     */
    public <T> Future<T> withTransaction(Function<SqlConnection, Future<T>> function) {
        return withConnection(sqlConnection -> {
            return sqlConnection.begin().compose(transaction -> {
                return Future.succeededFuture().compose(v -> {
                            // execute and commit
                            return function.apply(sqlConnection)
                                    .compose(t -> transaction
                                            .commit().compose(committed -> Future.succeededFuture(t)));
                        })
                        .compose(Future::succeededFuture, err -> {
                            if (err instanceof TransactionRollbackException) {
                                // already rollback
                                return Future.failedFuture(new KeelMySQLException("MySQLDataSource ROLLBACK Done Manually", err));
                            } else {
                                // since 3.0.3 rollback failure would be thrown directly to downstream.
                                return transaction.rollback()
                                        .compose(rollbackDone -> Future.failedFuture(new KeelMySQLException("MySQLDataSource ROLLBACK Finished", err)));
                            }
                        });
            }, beginFailure -> Future.failedFuture(new KeelMySQLConnectionException(
                    "MySQLDataSource Failed to get SqlConnection for transaction From Pool: " + beginFailure,
                    beginFailure
            )));
        });
        //return pool.withTransaction(function);
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
     * @since 3.0.5
     */
    public Future<Void> close() {
        return this.pool.close();
    }

    /**
     * @param ar
     * @since 3.0.5
     */
    public void close(Handler<AsyncResult<Void>> ar) {
        this.pool.close(ar);
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

        @Nonnull
        @Override
        public Iterator<Row> iterator() {
            return this.iteratorOverCursor;
        }
    }
}
