package io.github.sinri.keel.mysql;

import io.github.sinri.keel.mysql.exception.KeelMySQLConnectionException;
import io.github.sinri.keel.mysql.exception.KeelMySQLException;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.mysqlclient.MySQLBuilder;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.SqlConnection;
import io.vertx.sqlclient.TransactionRollbackException;

import javax.annotation.Nonnull;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import static io.github.sinri.keel.facade.KeelInstance.keel;

/**
 * Pair data source to a named mysql connection.
 *
 * @param <C>
 * @since 3.0.11
 * @since 3.0.18 Finished Technical Preview.
 */
public class NamedMySQLDataSource<C extends NamedMySQLConnection> {

    //    private final MySQLPool pool;
    private final Pool pool;
    private final KeelMySQLConfiguration configuration;
    /**
     * @since 3.0.2
     */
    private final AtomicInteger connectionAvailableCounter = new AtomicInteger(0);

    private final Function<SqlConnection, C> sqlConnectionWrapper;

    public NamedMySQLDataSource(
            KeelMySQLConfiguration configuration,
            Function<SqlConnection, C> sqlConnectionWrapper
    ) {
        this(configuration, sqlConnection -> Future.succeededFuture(), sqlConnectionWrapper);
    }

    /**
     * @since 3.0.2
     */
    public NamedMySQLDataSource(
            KeelMySQLConfiguration configuration,
            @Nonnull Function<SqlConnection, Future<Void>> connectionSetUpFunction,
            Function<SqlConnection, C> sqlConnectionWrapper
    ) {
        this.configuration = configuration;
        this.sqlConnectionWrapper = sqlConnectionWrapper;
        pool = MySQLBuilder.pool()
                .with(configuration.getPoolOptions())
                .connectingTo(configuration.getConnectOptions())
                .using(keel.getVertx())
                .withConnectHandler(sqlConnection -> {
                    connectionSetUpFunction.apply(sqlConnection)
                            .onComplete(ar -> {
                                connectionAvailableCounter.incrementAndGet();
                                sqlConnection.close();
                            });
                })
                .build();
//        pool = MySQLPool.pool(
//                keel.getVertx(),
//                configuration.getConnectOptions(),
//                configuration.getPoolOptions()
//        );
//        pool.connectHandler(sqlConnection -> {
//            connectionSetUpFunction.apply(sqlConnection)
//                    .onComplete(ar -> {
//                        connectionAvailableCounter.incrementAndGet();
//                        sqlConnection.close();
//                    });
//        });
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

    protected Future<C> fetchMySQLConnection() {
        return pool.getConnection()
                .compose(sqlConnection -> {
                    connectionAvailableCounter.decrementAndGet();
                    C c = this.sqlConnectionWrapper.apply(sqlConnection);
                    return Future.succeededFuture(c);
                }, throwable -> {
                    return Future.failedFuture(new KeelMySQLConnectionException(
                            "MySQLDataSource Failed to get SqlConnection From Pool " +
                                    "`" + this.getConfiguration().getDataSourceName() + "` " +
                                    "(available: " + connectionAvailableCounter.get() + "): " +
                                    throwable,
                            throwable
                    ));
                });
    }

    public <T> Future<T> withConnection(Function<C, Future<T>> function) {
        return fetchMySQLConnection()
                .compose(sqlConnectionWrapper -> {
                    return Future.succeededFuture()
                            .compose(v -> {
                                return function.apply(sqlConnectionWrapper);
                            })
                            .andThen(tAsyncResult -> {
                                sqlConnectionWrapper.getSqlConnection().close();
                                connectionAvailableCounter.incrementAndGet();
                            })
                            .recover(throwable -> {
                                return Future.failedFuture(new KeelMySQLException(
                                        "MySQLDataSource Failed Within SqlConnection: " + throwable,
                                        throwable
                                ));
                            });
                });
    }

    public <T> Future<T> withTransaction(Function<C, Future<T>> function) {
        return withConnection(c -> {
            return c.getSqlConnection().begin()
                    .compose(transaction -> {
                        return Future.succeededFuture()
                                .compose(v -> {
                                    // execute and commit
                                    return function.apply(c)
                                            .compose(t -> transaction.commit()
                                                    .compose(committed -> Future.succeededFuture(t)));
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
}
