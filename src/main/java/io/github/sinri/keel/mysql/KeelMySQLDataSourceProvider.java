package io.github.sinri.keel.mysql;

import io.github.sinri.keel.facade.Keel3;
import io.github.sinri.keel.facade.KeelConfiguration;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class KeelMySQLDataSourceProvider {

    private static final Map<String, MySQLDataSource> mySQLDataSourceMap = new ConcurrentHashMap<>();

    public static MySQLDataSource getMySQLDataSource() {
        String defaultName = defaultMySQLDataSourceName();
        return getMySQLDataSource(defaultName);
    }

    public static MySQLDataSource getMySQLDataSource(@Nonnull String dataSourceName) {
        if (!mySQLDataSourceMap.containsKey(dataSourceName)) {
            KeelConfiguration configuration = Keel3.getConfiguration().extract("mysql", dataSourceName);
            Objects.requireNonNull(configuration);
            KeelMySQLConfigure mySQLConfigure = new KeelMySQLConfigure(dataSourceName, configuration);
            MySQLDataSource mySQLDataSource = new MySQLDataSource(mySQLConfigure);
            mySQLDataSourceMap.put(dataSourceName, mySQLDataSource);
        }
        return mySQLDataSourceMap.get(dataSourceName);
    }

    @Nonnull
    public static String defaultMySQLDataSourceName() {
        return Objects.requireNonNullElse(Keel3.getConfiguration().readString("mysql", "default_data_source_name"), "default");
    }
}
