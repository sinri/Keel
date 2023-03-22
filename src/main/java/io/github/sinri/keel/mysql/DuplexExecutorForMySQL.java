package io.github.sinri.keel.mysql;

import io.github.sinri.keel.core.DuplexExecutor;
import io.vertx.sqlclient.SqlConnection;

import java.sql.SQLException;
import java.sql.Statement;

/**
 * 
 * @since 1.13
 * @deprecated since 2.1
 */
@Deprecated(since = "2.1")
public class DuplexExecutorForMySQL<T> extends DuplexExecutor<T, SqlConnection, Statement, SQLException> {
    public DuplexExecutorForMySQL(
            AsyncExecutor<T, SqlConnection> asyncExecutor,
            SyncExecutor<T, Statement, SQLException> syncExecutor
    ) {
        super(asyncExecutor, syncExecutor);
    }
}
