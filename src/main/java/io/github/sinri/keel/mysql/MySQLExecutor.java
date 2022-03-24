package io.github.sinri.keel.mysql;

import io.github.sinri.keel.Keel;
import io.vertx.core.Future;
import io.vertx.sqlclient.SqlConnection;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.function.Function;

/**
 * @param <T> the result value class
 * @since 1.10
 */
public class MySQLExecutor<T> {
    protected AsyncMySQLExecutor<T> asyncMySQLExecutor;
    protected SyncMySQLExecutor<T> syncMySQLExecutor;

    private SqlConnection lastSqlConnection;
    private Statement lastStatement;
    private T lastResult;

    public static <R> MySQLExecutor<R> build(AsyncMySQLExecutor<R> asyncMySQLExecutor, SyncMySQLExecutor<R> syncMySQLExecutor) {
        MySQLExecutor<R> duplexExecutorForMySQL = new MySQLExecutor<>();
        duplexExecutorForMySQL.setAsyncMySQLExecutor(asyncMySQLExecutor);
        duplexExecutorForMySQL.setSyncMySQLExecutor(syncMySQLExecutor);
        return duplexExecutorForMySQL;
    }

    public static <R> MySQLExecutor<R> buildForNull(Class<R> classOfR) {
        return MySQLExecutor.build(sqlConnection -> Future.succeededFuture(null), statement -> null);
    }

    public static <R> MySQLExecutor<R> buildForFixed(R fixed) {
        return MySQLExecutor.build(sqlConnection -> Future.succeededFuture(fixed), statement -> fixed);
    }

    protected void setAsyncMySQLExecutor(AsyncMySQLExecutor<T> asyncMySQLExecutor) {
        this.asyncMySQLExecutor = asyncMySQLExecutor;
    }

    protected void setSyncMySQLExecutor(SyncMySQLExecutor<T> syncMySQLExecutor) {
        this.syncMySQLExecutor = syncMySQLExecutor;
    }

    public final Future<T> executeAsync(SqlConnection sqlConnection) {
        if (this.asyncMySQLExecutor != null) {
            lastSqlConnection = sqlConnection;
            return this.asyncMySQLExecutor.execute(sqlConnection)
                    .compose(t -> {
                        lastResult = t;
                        return Future.succeededFuture(t);
                    });
        }
        throw new RuntimeException(getClass().getName() + "::executeAsync asyncMySQLExecutor not initialized");
    }

    /**
     * Read properties for "mysql.default_data_source_name",
     * the value would be used to determine which mysql vertx instance to get the current sql connection
     *
     * @return future for result
     * Note this would not be put into any MySQL transaction!
     */
    public Future<T> executeAsync() {
        if (this.asyncMySQLExecutor != null) {
            return Keel.getMySQLKit().getPool().withConnection(this::executeAsync);
        }
        throw new RuntimeException(getClass().getName() + "::executeAsync asyncMySQLExecutor not initialized");
    }

    public final T executeSync(Statement statement) throws SQLException {
        if (this.syncMySQLExecutor != null) {
            lastStatement = statement;
            T t = this.syncMySQLExecutor.execute(statement);
            lastResult = t;
            return t;
        }
        throw new RuntimeException(getClass().getName() + "::executeAsync syncMySQLExecutor not initialized");
    }

    /**
     * Read properties for "mysql.default_data_source_name",
     * the value would be used to determine which mysql JDBC instance to get the current statement
     *
     * @return MySQL Execute Result
     * @throws SQLException when SQL error
     */
    public T executeSync() throws SQLException {
        if (this.syncMySQLExecutor != null) {
            Statement statement = Keel.getMySQLKitWithJDBC().getThreadLocalStatementWrapper().getCurrentThreadLocalStatement();
            return executeSync(statement);
        }
        throw new RuntimeException(getClass().getName() + "::executeAsync syncMySQLExecutor not initialized");
    }

    public interface AsyncMySQLExecutor<R> {
        Future<R> execute(SqlConnection sqlConnection);
    }

    public interface SyncMySQLExecutor<R> {
        R execute(Statement statement) throws SQLException;
    }

    /**
     * @return SqlConnection
     * @since 1.13
     */
    public SqlConnection getLastSqlConnection() {
        return lastSqlConnection;
    }

    /**
     * @return Statement
     * @since 1.13
     */
    public Statement getLastStatement() {
        return lastStatement;
    }

    /**
     * @return T
     * @since 1.13
     */
    public T getLastResult() {
        return lastResult;
    }

    /**
     * It should be declared before calling `execute*`,
     * implemented using method `getLastSqlConnection()`,`getLastStatement()`, and `getLastResult()`.
     *
     * @param nextAsyncFunction async
     * @param nextSyncFunction  sync
     * @param <K>               next result type
     * @return MySQLExecutor with K
     * @since 1.13
     */
    public <K> MySQLExecutor<K> downstream(
            Function<MySQLExecutor<T>, Future<K>> nextAsyncFunction,
            Function<MySQLExecutor<T>, K> nextSyncFunction
    ) {
        var upstream = this;
        return MySQLExecutor.build(
                sqlConnection -> upstream.executeAsync(sqlConnection)
                        .compose(t -> nextAsyncFunction.apply(upstream)),
                statement -> {
                    upstream.executeSync(statement);
                    return nextSyncFunction.apply(upstream);
                }
        );
    }
}
