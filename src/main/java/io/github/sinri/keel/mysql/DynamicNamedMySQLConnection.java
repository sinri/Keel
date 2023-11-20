package io.github.sinri.keel.mysql;

import io.github.sinri.keel.core.TechnicalPreview;
import io.vertx.sqlclient.SqlConnection;

import javax.annotation.Nonnull;

@TechnicalPreview(since = "3.0.11", notice = "To avoid mix in multi-data-sources")
public class DynamicNamedMySQLConnection extends NamedMySQLConnection {
    private final String dataSourceName;

    public DynamicNamedMySQLConnection(SqlConnection sqlConnection, String dataSourceName) {
        super(sqlConnection);
        this.dataSourceName = dataSourceName;
    }

    @Nonnull
    @Override
    public String getDataSourceName() {
        return dataSourceName;
    }

}
