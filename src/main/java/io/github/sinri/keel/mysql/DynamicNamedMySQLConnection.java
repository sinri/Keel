package io.github.sinri.keel.mysql;

import io.vertx.sqlclient.SqlConnection;

import javax.annotation.Nonnull;

/**
 * @since 3.0.11 Technical Preview. To avoid mix in multi-data-sources.
 * @since 3.0.18 Finished Technical Preview.
 */
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
