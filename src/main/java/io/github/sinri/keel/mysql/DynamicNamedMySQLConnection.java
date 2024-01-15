package io.github.sinri.keel.mysql;

import io.vertx.sqlclient.SqlConnection;

import javax.annotation.Nonnull;

/**
 * @since 3.0.11 Technical Preview. To avoid mix in multi-data-sources.
 * @since 3.0.18 Finished Technical Preview.
 */
public class DynamicNamedMySQLConnection extends NamedMySQLConnection {
    private final @Nonnull  String dataSourceName;

    public DynamicNamedMySQLConnection(@Nonnull SqlConnection sqlConnection, @Nonnull String dataSourceName) {
        super(sqlConnection);
        this.dataSourceName = dataSourceName;
    }

    @Nonnull
    @Override
    public String getDataSourceName() {
        return dataSourceName;
    }

}
