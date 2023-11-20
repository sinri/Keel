package io.github.sinri.keel.mysql;

import io.github.sinri.keel.core.DuplexExecutor;
import io.vertx.sqlclient.SqlConnection;

import java.sql.SQLException;
import java.sql.Statement;

/**
 * @param <T>
 * @since 1.13
 * @deprecated since 2.1
 */
@Deprecated(since = "2.1", forRemoval = true)
public class DuplexExecutorForMySQL<T> extends DuplexExecutor<T, SqlConnection, Statement, SQLException> {
    public DuplexExecutorForMySQL(
            AsyncExecutor<T, SqlConnection> asyncExecutor,
            SyncExecutor<T, Statement, SQLException> syncExecutor
    ) {
        super(asyncExecutor, syncExecutor);
    }
}
