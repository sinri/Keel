package io.github.sinri.keel.mysql;

import io.vertx.sqlclient.SqlConnection;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @since 3.0.11 Technical Preview. To avoid mix in multi-data-sources.
 * @since 3.0.18 Finished Technical Preview.
 */
abstract public class NamedMySQLConnection {
    private final SqlConnection sqlConnection;

    public NamedMySQLConnection(SqlConnection sqlConnection) {
        this.sqlConnection = sqlConnection;
    }

    public SqlConnection getSqlConnection() {
        return sqlConnection;
    }

    /**
     * @return The data source which provided the sql connection.
     */
    @Nonnull
    abstract public String getDataSourceName();

    private @Nullable String mysqlVersion;

    @Nullable
    public final String getMysqlVersion() {
        return mysqlVersion;
    }

    public final NamedMySQLConnection setMysqlVersion(@Nullable String mysqlVersion) {
        this.mysqlVersion = mysqlVersion;
        return this;
    }

    public final boolean isMySQLVersion5dot6() {
        return mysqlVersion != null
                && mysqlVersion.startsWith("5.6.");
    }

    public final boolean isMySQLVersion5dot7() {
        return mysqlVersion != null
                && mysqlVersion.startsWith("5.7.");
    }

    public final boolean isMySQLVersion8dot0() {
        return mysqlVersion != null
                && mysqlVersion.startsWith("8.0.");
    }

    public final boolean isMySQLVersion8dot2() {
        return mysqlVersion != null
                && mysqlVersion.startsWith("8.2.");
    }
}
