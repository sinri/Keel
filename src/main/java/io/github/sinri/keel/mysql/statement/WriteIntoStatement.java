package io.github.sinri.keel.mysql.statement;

import io.github.sinri.keel.core.KeelHelper;
import io.github.sinri.keel.mysql.KeelMySQLQuoter;
import io.github.sinri.keel.mysql.jdbc.KeelJDBCForMySQL;
import io.github.sinri.keel.mysql.matrix.ResultMatrix;
import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.sqlclient.SqlConnection;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class WriteIntoStatement extends AbstractStatement {
    /**
     * insert [ignore] into schema.table (column...) values (value...),... ON DUPLICATE KEY UPDATE assignment_list
     * insert [ignore] into schema.table (column...) [select ...| table ...] ON DUPLICATE KEY UPDATE assignment_list
     */

    public static final String INSERT = "INSERT";
    public static final String REPLACE = "REPLACE";
    final List<String> columns = new ArrayList<>();
    final List<List<String>> batchValues = new ArrayList<>();
    final Map<String, String> onDuplicateKeyUpdateAssignmentMap = new HashMap<>();
    String writeType = INSERT;
    String ignoreMark = "";
    String schema;
    String table;
    String sourceSelectSQL;
    String sourceTableName;

    public WriteIntoStatement() {

    }

    public WriteIntoStatement(String writeType) {
        this.writeType = writeType;
    }

    public WriteIntoStatement intoTable(String table) {
        this.table = table;
        return this;
    }

    public WriteIntoStatement intoTable(String schema, String table) {
        this.schema = schema;
        this.table = table;
        return this;
    }

    public WriteIntoStatement ignore() {
        this.ignoreMark = "IGNORE";
        return this;
    }

    public WriteIntoStatement columns(List<String> columns) {
        this.columns.addAll(columns);
        return this;
    }

    public WriteIntoStatement addDataMatrix(List<List<Object>> batch) {
        for (List<Object> row : batch) {
            List<String> t = new ArrayList<>();
            for (Object item : row) {
                t.add(new KeelMySQLQuoter(String.valueOf(item)).toString());
            }
            this.batchValues.add(t);
        }
        return this;
    }

    public WriteIntoStatement addDataRow(List<Object> row) {
        List<String> t = new ArrayList<>();
        for (Object item : row) {
            t.add(new KeelMySQLQuoter(String.valueOf(item)).toString());
        }
        this.batchValues.add(t);
        return this;
    }

    /**
     * @param row One Json Object for one row
     * @return WriteIntoStatement
     * @since 1.7
     */
    public WriteIntoStatement macroWriteOneRowWithJsonObject(JsonObject row) {
        columns.clear();
        this.batchValues.clear();
        List<String> dataRow = new ArrayList<>();
        row.forEach(entry -> {
            columns.add(entry.getKey());
            dataRow.add(new KeelMySQLQuoter(entry.getValue().toString()).toString());
        });
        this.batchValues.add(dataRow);
        return this;
    }

    /**
     * @param mapForOneRow map for one row
     * @return WriteIntoStatement
     * @since 1.6
     */
    public WriteIntoStatement macroWriteOneRowWithMap(Map<String, Object> mapForOneRow) {
        columns.clear();
        this.batchValues.clear();
        List<String> dataRow = new ArrayList<>();
        mapForOneRow.forEach((key, value) -> {
            columns.add(key);
            dataRow.add(new KeelMySQLQuoter(String.valueOf(value)).toString());
        });
        this.batchValues.add(dataRow);
        return this;
    }

    /**
     * @param rows Json Array contains rows, one Json Object for one row
     * @return WriteIntoStatement
     * @since 1.7
     */
    public WriteIntoStatement macroWriteRowsWithMapList(JsonArray rows) {
        columns.clear();
        this.batchValues.clear();
        AtomicBoolean isFirstRow = new AtomicBoolean(true);
        rows.forEach(map -> {
            List<String> dataRow = new ArrayList<>();

            if (map instanceof JsonObject) {
                ((JsonObject) map).forEach(entry -> {
                    if (isFirstRow.get()) columns.add(entry.getKey());
                    dataRow.add(new KeelMySQLQuoter(entry.getValue().toString()).toString());
                });
                isFirstRow.set(false);
            }
            this.batchValues.add(dataRow);
        });

        return this;
    }

    /**
     * @param mapListForRows map list for rows
     * @return WriteIntoStatement
     * @since 1.6
     */
    public WriteIntoStatement macroWriteRowsWithMapList(List<Map<String, Object>> mapListForRows) {
        columns.clear();
        this.batchValues.clear();
        AtomicBoolean isFirstRow = new AtomicBoolean(true);

        mapListForRows.forEach(map -> {
            List<String> dataRow = new ArrayList<>();
            map.forEach((key, value) -> {
                if (isFirstRow.get()) columns.add(key);
                dataRow.add(new KeelMySQLQuoter(String.valueOf(value)).toString());
            });
            this.batchValues.add(dataRow);
            isFirstRow.set(false);
        });

        return this;
    }

    public WriteIntoStatement fromSelection(String selectionSQL) {
        this.sourceSelectSQL = selectionSQL;
        return this;
    }

    public WriteIntoStatement fromTable(String tableName) {
        this.sourceTableName = tableName;
        return this;
    }

    public WriteIntoStatement onDuplicateKeyUpdate(String column, String updateExpression) {
        this.onDuplicateKeyUpdateAssignmentMap.put(column, updateExpression);
        return this;
    }

    public String toString() {
        String sql = writeType + " " + ignoreMark + " INTO ";
        if (schema != null) {
            sql += schema + ".";
        }
        sql += table;
        sql += " (" + KeelHelper.joinStringArray(columns, ",") + ")";
        if (sourceTableName != null) {
            sql += AbstractStatement.SQL_COMPONENT_SEPARATOR + "TABLE " + sourceTableName;
        } else if (sourceSelectSQL != null) {
            sql += AbstractStatement.SQL_COMPONENT_SEPARATOR + sourceSelectSQL;
        } else {
            sql += AbstractStatement.SQL_COMPONENT_SEPARATOR + "VALUES" + AbstractStatement.SQL_COMPONENT_SEPARATOR;
            List<String> items = new ArrayList<>();
            for (List<String> row : batchValues) {
                items.add("(" + KeelHelper.joinStringArray(row, ",") + ")");
            }
            sql += KeelHelper.joinStringArray(items, "," + AbstractStatement.SQL_COMPONENT_SEPARATOR);
        }
        if (!onDuplicateKeyUpdateAssignmentMap.isEmpty()) {
            sql += AbstractStatement.SQL_COMPONENT_SEPARATOR + "ON DUPLICATE KEY UPDATE" + AbstractStatement.SQL_COMPONENT_SEPARATOR;
            List<String> items = new ArrayList<>();
            onDuplicateKeyUpdateAssignmentMap.forEach((key, value) -> items.add(key + " = " + value));
            sql += KeelHelper.joinStringArray(items, "," + AbstractStatement.SQL_COMPONENT_SEPARATOR);
        }
        return sql;
    }

    /**
     * @param sqlConnection get from pool
     * @return future with affected rows; if any error occurs, failed future returned instead.
     * @since 1.7
     * @since 1.10, removed the recover block
     */
    public Future<Integer> executeForAffectedRows(SqlConnection sqlConnection) {
        return execute(sqlConnection)
                .compose(resultMatrix -> Future.succeededFuture(resultMatrix.getTotalAffectedRows()))
//                .recover(throwable -> {
//                    Keel.outputLogger("MySQL").warning(getClass().getName() + " executeForAffectedRows failed [" + throwable.getMessage() + "] when executing SQL: " + this);
//                    return Future.succeededFuture(-1);
//                })
                ;
    }

    /**
     * @param sqlConnection get from pool
     * @return future with last inserted id; if any error occurs, failed future returned instead.
     * @since 1.7
     * @since 1.10, removed the recover block
     */
    public Future<Long> executeForLastInsertedID(SqlConnection sqlConnection) {
        return execute(sqlConnection)
                .compose(resultMatrix -> Future.succeededFuture(resultMatrix.getLastInsertedID()))
//                .recover(throwable -> {
//                    Keel.outputLogger("MySQL").warning(getClass().getName() + " executeForLastInsertedID failed [" + throwable.getMessage() + "] when executing SQL: " + this);
//                    return Future.succeededFuture(-1L);
//                })
                ;
    }

    @Override
    public ResultMatrix blockedExecute(Statement statement) throws SQLException {
        return KeelJDBCForMySQL.executeForInsertion(this.toString(), statement);
    }
}
