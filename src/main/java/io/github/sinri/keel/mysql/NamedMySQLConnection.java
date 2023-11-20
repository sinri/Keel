package io.github.sinri.keel.mysql;

import io.github.sinri.keel.core.TechnicalPreview;
import io.vertx.sqlclient.SqlConnection;

@TechnicalPreview(since = "3.0.11", notice = "To avoid mix in multi-data-sources")
abstract public class NamedMySQLConnection {
    private final SqlConnection sqlConnection;
    private final String dataSourceName;

    public NamedMySQLConnection(String dataSourceName, SqlConnection sqlConnection) {
        this.sqlConnection = sqlConnection;
        this.dataSourceName = dataSourceName;
    }

    public SqlConnection getSqlConnection() {
        return sqlConnection;
    }

    public String getDataSourceName() {
        return this.dataSourceName;
    }
}
