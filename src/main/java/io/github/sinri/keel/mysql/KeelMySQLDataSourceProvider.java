package io.github.sinri.keel.mysql;

import io.vertx.sqlclient.SqlConnection;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;

import static io.github.sinri.keel.facade.KeelInstance.Keel;

public class KeelMySQLDataSourceProvider {

    @NotNull
    public static String defaultMySQLDataSourceName() {
        return Objects.requireNonNull(Keel.getConfiguration().readString(List.of("mysql", "default_data_source_name"), "default"));
    }

    /**
     * @since 3.0.11 Technical Preview.
     * @since 3.0.18 Finished Technical Preview.
     */
    public static <C extends NamedMySQLConnection> NamedMySQLDataSource<C> initializeNamedMySQLDataSource(
            @NotNull String dataSourceName,
            Function<SqlConnection, C> sqlConnectionWrapper
    ) {
        var configuration = Keel.getConfiguration().extract("mysql", dataSourceName);
        Objects.requireNonNull(configuration);
        KeelMySQLConfiguration mySQLConfigure = new KeelMySQLConfiguration(configuration);
        return new NamedMySQLDataSource<>(mySQLConfigure, sqlConnectionWrapper);
    }

    /**
     * @since 3.0.11 Technical Preview.
     * @since 3.0.18 Finished Technical Preview.
     */
    public static NamedMySQLDataSource<DynamicNamedMySQLConnection> initializeDynamicNamedMySQLDataSource(@NotNull String dataSourceName) {
        return initializeNamedMySQLDataSource(dataSourceName, sqlConnection -> new DynamicNamedMySQLConnection(sqlConnection, dataSourceName));
    }
}
