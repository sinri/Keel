package io.github.sinri.keel.mysql;

import io.vertx.core.Future;

import javax.annotation.Nonnull;

public interface MySQLDataSourceProvider {
    Future<Void> initializeMySQLDataSource(@Nonnull String dataSourceName);

    MySQLDataSource getMySQLDataSource(@Nonnull String dataSourceName);

    @Nonnull
    String defaultMySQLDataSourceName();

    default MySQLDataSource getMySQLDataSource() {
        String defaultName = defaultMySQLDataSourceName();
        return getMySQLDataSource(defaultName);
    }
}
