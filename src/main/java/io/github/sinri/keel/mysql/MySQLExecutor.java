package io.github.sinri.keel.mysql;

import io.github.sinri.keel.Keel;
import io.vertx.core.Future;
import io.vertx.sqlclient.SqlConnection;

import java.sql.SQLException;
import java.sql.Statement;

/**
 * @param <T> the result value class
 * @since 1.10
 * @deprecated
 */
@Deprecated
public class MySQLExecutor<T> {
    protected AsyncMySQLExecutor<T> asyncMySQLExecutor;
    protected SyncMySQLExecutor<T> syncMySQLExecutor;

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
            return this.asyncMySQLExecutor.execute(sqlConnection);
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
            return this.syncMySQLExecutor.execute(statement);
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
}
