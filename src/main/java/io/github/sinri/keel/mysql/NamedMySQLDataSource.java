package io.github.sinri.keel.mysql;

import io.github.sinri.keel.mysql.exception.KeelMySQLConnectionException;
import io.github.sinri.keel.mysql.exception.KeelMySQLException;
import io.github.sinri.keel.mysql.matrix.ResultMatrix;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.mysqlclient.MySQLBuilder;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.SqlConnection;
import io.vertx.sqlclient.TransactionRollbackException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

import static io.github.sinri.keel.facade.KeelInstance.Keel;

/**
 * Pair data source to a named mysql connection.
 *
 * @param <C>
 * @since 3.0.11
 * @since 3.0.18 Finished Technical Preview.
 */
public class NamedMySQLDataSource<C extends NamedMySQLConnection> {

    private final Pool pool;
    private final KeelMySQLConfiguration configuration;
    /**
     * @since 3.0.2
     */
    private final AtomicInteger connectionAvailableCounter = new AtomicInteger(0);

    private final Function<SqlConnection, C> sqlConnectionWrapper;

    private final AtomicReference<String> fullVersionRef = new AtomicReference<>(null);

    public NamedMySQLDataSource(
            @Nonnull KeelMySQLConfiguration configuration,
            @Nonnull Function<SqlConnection, C> sqlConnectionWrapper
    ) {
        this(configuration, sqlConnection -> Future.succeededFuture(), sqlConnectionWrapper);
    }

    /**
     * @since 3.0.2
     */
    public NamedMySQLDataSource(
            @Nonnull KeelMySQLConfiguration configuration,
            @Nullable Function<SqlConnection, Future<Void>> connectionSetUpFunction,
            @Nonnull Function<SqlConnection, C> sqlConnectionWrapper
    ) {
        this.configuration = configuration;
        this.sqlConnectionWrapper = sqlConnectionWrapper;
        pool = MySQLBuilder.pool()
                .with(configuration.getPoolOptions())
                .connectingTo(configuration.getConnectOptions())
                .using(Keel.getVertx())
                .withConnectHandler(sqlConnection -> {
                    Future.succeededFuture()
                            .compose(v -> {
                                if (connectionSetUpFunction != null) {
                                    return connectionSetUpFunction.apply(sqlConnection);
                                } else {
                                    return Future.succeededFuture();
                                }
                            })
                            .compose(v -> {
                                if (this.fullVersionRef.get() == null) {
                                    return checkMySQLVersion(sqlConnection)
                                            .compose(ver -> {
                                                if (ver != null) {
                                                    this.fullVersionRef.set(ver);
                                                }
                                                return Future.succeededFuture();
                                            });
                                } else {
                                    return Future.succeededFuture();
                                }
                            })
                            .onComplete(ar -> {
                                connectionAvailableCounter.incrementAndGet();
                                sqlConnection.close();
                            });
                })
                .build();
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
     * @since 3.1.0
     */
    private static Future<String> checkMySQLVersion(@Nonnull SqlConnection sqlConnection) {
        return sqlConnection.preparedQuery("SELECT VERSION() as v; ")
                .execute()
                .compose(rows -> {
                    return Future.succeededFuture(ResultMatrix.create(rows));
                })
                .compose(resultMatrix -> {
                    try {
                        JsonObject firstRow = resultMatrix.getFirstRow();
                        String versionExp = firstRow.getString("v");
                        return Future.succeededFuture(versionExp);
                    } catch (Throwable e) {
                        Keel.getLogger().exception(e);
                        return Future.succeededFuture(null);
                    }
                });
    }

    /**
     * @since 3.1.0
     */
    public @Nullable String getFullVersionRef() {
        return fullVersionRef.get();
    }

    public <T> Future<T> withConnection(@Nonnull Function<C, Future<T>> function) {
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

    public <T> Future<T> withTransaction(@Nonnull Function<C, Future<T>> function) {
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
     * @since 3.0.5
     */
    public void close(@Nonnull Handler<AsyncResult<Void>> ar) {
        this.pool.close(ar);
    }

    protected Future<C> fetchMySQLConnection() {
        return pool.getConnection()
                .compose(sqlConnection -> {
                    connectionAvailableCounter.decrementAndGet();
                    C c = this.sqlConnectionWrapper.apply(sqlConnection);

                    // since 3.1.0: add mysql version to c;
                    c.setMysqlVersion(this.fullVersionRef.get());

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
}
