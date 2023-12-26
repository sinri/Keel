package io.github.sinri.keel.mysql;

import io.vertx.sqlclient.SqlConnection;

import javax.annotation.Nonnull;

/**
 * @since 3.0.11 Technical Preview. To avoid mix in multi-data-sources.
 * @since 3.0.18 Finished Technical Preview.
 */
abstract public class NamedMySQLConnection {
    private final SqlConnection sqlConnection;

    public NamedMySQLConnection(SqlConnection sqlConnection) {
        this.sqlConnection = sqlConnection;
    }

    public SqlConnection getSqlConnection() {
        return sqlConnection;
    }

    /**
     * @return The data source which provided the sql connection.
     */
    @Nonnull
    abstract public String getDataSourceName();
}
