package io.github.sinri.keel.mysql;

import io.vertx.sqlclient.SqlConnection;

import javax.annotation.Nonnull;

/**
 * @since 3.0.1
 */
@Deprecated(since = "3.1.0",forRemoval = true)
abstract public class AbstractAction {
    private final @Nonnull SqlConnection sqlConnection;

    public AbstractAction(@Nonnull SqlConnection sqlConnection) {
        this.sqlConnection = sqlConnection;
    }

    protected @Nonnull SqlConnection getSqlConnection() {
        return sqlConnection;
    }
}
