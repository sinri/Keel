package io.github.sinri.keel.mysql;

import io.github.sinri.keel.core.TechnicalPreview;
import io.vertx.sqlclient.SqlConnection;

import javax.annotation.Nonnull;

@TechnicalPreview(since = "3.0.11", notice = "To avoid mix in multi-data-sources")
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
