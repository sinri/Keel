package io.github.sinri.keel.mysql;

import io.github.sinri.keel.facade.KeelConfiguration;
import io.vertx.sqlclient.SqlConnection;

import javax.annotation.Nonnull;
import java.util.Objects;
import java.util.function.Function;

import static io.github.sinri.keel.facade.KeelInstance.Keel;

public class KeelMySQLDataSourceProvider {

    @Nonnull
    public static String defaultMySQLDataSourceName() {
        return Objects.requireNonNullElse(Keel.getConfiguration().readString("mysql", "default_data_source_name"), "default");
    }

    /**
     * @since 3.0.11 Technical Preview.
     * @since 3.0.18 Finished Technical Preview.
     */
    public static <C extends NamedMySQLConnection> NamedMySQLDataSource<C> initializeNamedMySQLDataSource(
            @Nonnull String dataSourceName,
            Function<SqlConnection, C> sqlConnectionWrapper
    ) {
        KeelConfiguration configuration = Keel.getConfiguration().extract("mysql", dataSourceName);
        Objects.requireNonNull(configuration);
        KeelMySQLConfiguration mySQLConfigure = new KeelMySQLConfiguration(dataSourceName, configuration);
        return new NamedMySQLDataSource<>(mySQLConfigure, sqlConnectionWrapper);
    }

    /**
     * @since 3.0.11 Technical Preview.
     * @since 3.0.18 Finished Technical Preview.
     */
    public static NamedMySQLDataSource<DynamicNamedMySQLConnection> initializeDynamicNamedMySQLDataSource(@Nonnull String dataSourceName) {
        return initializeNamedMySQLDataSource(dataSourceName, sqlConnection -> new DynamicNamedMySQLConnection(sqlConnection, dataSourceName));
    }
}
