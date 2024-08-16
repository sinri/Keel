package io.github.sinri.keel.mysql;

import io.vertx.sqlclient.SqlConnection;
import org.jetbrains.annotations.NotNull;


/**
 * @since 3.0.11 Technical Preview. To avoid mix in multi-data-sources.
 * @since 3.0.18 Finished Technical Preview.
 */
public class DynamicNamedMySQLConnection extends NamedMySQLConnection {
    private final @NotNull String dataSourceName;

    public DynamicNamedMySQLConnection(@NotNull SqlConnection sqlConnection, @NotNull String dataSourceName) {
        super(sqlConnection);
        this.dataSourceName = dataSourceName;
    }


    @Override
    public @NotNull String getDataSourceName() {
        return dataSourceName;
    }
}
