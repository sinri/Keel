package io.github.sinri.Keel.mysql.SQLBuilder;

import io.vertx.sqlclient.SqlConnection;

/**
 * @deprecated
 */
abstract public class KeelMySQLTable {
    protected String tableName;
    protected String schemaName;
    protected SqlConnection sqlConnection;

    public KeelMySQLTable(SqlConnection sqlConnection, String schemaName, String tableName) {
        this.sqlConnection = sqlConnection;
        this.schemaName = schemaName;
        this.tableName = tableName;
    }

    public KeelMySQLTable(SqlConnection sqlConnection, String tableName) {
        this.sqlConnection = sqlConnection;
        this.schemaName = null;
        this.tableName = tableName;
    }

    public SqlConnection getSqlConnection() {
        return sqlConnection;
    }
}
