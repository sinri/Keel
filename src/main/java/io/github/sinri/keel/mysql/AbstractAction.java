package io.github.sinri.keel.mysql;

import io.vertx.sqlclient.SqlConnection;

/**
 * @since 3.0.1
 */
abstract public class AbstractAction {
    private final SqlConnection sqlConnection;

    public AbstractAction(SqlConnection sqlConnection) {
        this.sqlConnection = sqlConnection;
    }

    protected SqlConnection getSqlConnection() {
        return sqlConnection;
    }
}
