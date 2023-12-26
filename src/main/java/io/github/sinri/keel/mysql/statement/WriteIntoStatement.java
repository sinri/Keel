package io.github.sinri.keel.mysql.statement;

import io.github.sinri.keel.helper.KeelHelpers;
import io.github.sinri.keel.mysql.NamedMySQLConnection;
import io.github.sinri.keel.mysql.Quoter;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.sqlclient.SqlConnection;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

public class WriteIntoStatement extends AbstractModifyStatement {
    /**
     * insert [ignore] into schema.table (column...) values (value...),... ON DUPLICATE KEY UPDATE assignment_list
     * insert [ignore] into schema.table (column...) [select ...| table ...] ON DUPLICATE KEY UPDATE assignment_list
     */

    public static final String INSERT = "INSERT";
    public static final String REPLACE = "REPLACE";
    final List<String> columns = new ArrayList<>();
    final List<List<String>> batchValues = new ArrayList<>();
    final Map<String, String> onDuplicateKeyUpdateAssignmentMap = new HashMap<>();
    final String writeType;
    String ignoreMark = "";
    String schema;
    String table;
    String sourceSelectSQL;
    String sourceTableName;

    public WriteIntoStatement() {
        this.writeType = INSERT;
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
                if (item == null) {
                    t.add("NULL");
                } else {
                    t.add(new Quoter(String.valueOf(item)).toString());
                }
            }
            this.batchValues.add(t);
        }
        return this;
    }

    public WriteIntoStatement addDataRow(List<Object> row) {
        List<String> t = new ArrayList<>();
        for (Object item : row) {
            if (item == null) {
                t.add("NULL");
            } else {
                t.add(new Quoter(String.valueOf(item)).toString());
            }
        }
        this.batchValues.add(t);
        return this;
    }

    /**
     * @since 3.0.0
     */
    public WriteIntoStatement macroWriteRows(Collection<RowToWrite> rows) {
        if (rows == null || rows.isEmpty()) {
            throw new RuntimeException();
        }
        columns.clear();
        this.batchValues.clear();

        rows.forEach(row -> {
            if (row.map.isEmpty()) {
                throw new RuntimeException();
            }

            List<String> dataRow = new ArrayList<>();

            if (columns.isEmpty()) {
                columns.addAll(row.map.keySet());
            }

            columns.forEach(key -> {
                var value = row.map.get(key);
                dataRow.add(value);
            });

            this.batchValues.add(dataRow);
        });

        return this;
    }

    /**
     * @since 3.0.0
     */
    public WriteIntoStatement macroWriteOneRow(RowToWrite row) {
        columns.clear();
        this.batchValues.clear();
        List<String> dataRow = new ArrayList<>();
        row.map.forEach((column, expression) -> {
            columns.add(column);
            dataRow.add(expression);
        });
        this.batchValues.add(dataRow);
        return this;
    }

    /**
     * @since 3.0.0
     */
    public WriteIntoStatement macroWriteOneRow(Handler<RowToWrite> rowEditor) {
        RowToWrite rowToWrite = new RowToWrite();
        rowEditor.handle(rowToWrite);
        return macroWriteOneRow(rowToWrite);
    }

    /**
     * @param row One Json Object for one row
     * @return WriteIntoStatement
     * @since 1.7
     */
    @Deprecated(since = "3.0.0")
    public WriteIntoStatement macroWriteOneRowWithJsonObject(JsonObject row) {
        columns.clear();
        this.batchValues.clear();
        List<String> dataRow = new ArrayList<>();
        row.forEach(entry -> {
            columns.add(entry.getKey());
            if (entry.getValue() == null) {
                dataRow.add("NULL");
            } else {
                dataRow.add(new Quoter(entry.getValue().toString()).toString());
            }
        });
        this.batchValues.add(dataRow);
        return this;
    }

    /**
     * @param mapForOneRow map for one row
     * @return WriteIntoStatement
     * @since 1.6
     */
    @Deprecated(since = "3.0.0")
    public WriteIntoStatement macroWriteOneRowWithMap(Map<String, Object> mapForOneRow) {
        columns.clear();
        this.batchValues.clear();
        List<String> dataRow = new ArrayList<>();
        mapForOneRow.forEach((key, value) -> {
            columns.add(key);
            if (value == null) {
                dataRow.add("NULL");
            } else {
                dataRow.add(new Quoter(String.valueOf(value)).toString());
            }
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
                    if (entry.getValue() == null) {
                        dataRow.add("NULL");
                    } else {
                        dataRow.add(new Quoter(entry.getValue().toString()).toString());
                    }
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
                if (value == null) {
                    dataRow.add("NULL");
                } else {
                    dataRow.add(new Quoter(String.valueOf(value)).toString());
                }
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

    /**
     * @param fieldName the raw column name
     * @return as `onDuplicateKeyUpdate` does
     * @since 1.10
     */
    public WriteIntoStatement onDuplicateKeyUpdateField(String fieldName) {
        return this.onDuplicateKeyUpdate(fieldName, "values(" + fieldName + ")");
    }

    /**
     * @param fieldNameList the raw column name list
     * @return as `onDuplicateKeyUpdate` does
     * @since 1.10
     */
    public WriteIntoStatement onDuplicateKeyUpdateFields(List<String> fieldNameList) {
        for (var fieldName : fieldNameList) {
            this.onDuplicateKeyUpdate(fieldName, "values(" + fieldName + ")");
        }
        return this;
    }

    /**
     * @param fieldName the raw column name
     * @return as `onDuplicateKeyUpdate` does
     * @since 1.10
     */
    public WriteIntoStatement onDuplicateKeyUpdateExceptField(String fieldName) {
        for (var x : columns) {
            if (x.equalsIgnoreCase(fieldName)) {
                continue;
            }
            this.onDuplicateKeyUpdate(x, "values(" + x + ")");
        }
        return this;
    }

    /**
     * @param fieldNameList the raw column name list
     * @return as `onDuplicateKeyUpdate` does
     * @since 1.10
     */
    public WriteIntoStatement onDuplicateKeyUpdateExceptFields(List<String> fieldNameList) {
        for (var x : columns) {
            if (fieldNameList.contains(x)) continue;
            this.onDuplicateKeyUpdate(x, "values(" + x + ")");
        }
        return this;
    }

    public String toString() {
        String sql = writeType + " " + ignoreMark + " INTO ";
        if (schema != null) {
            sql += schema + ".";
        }
        sql += table;
        sql += " (" + KeelHelpers.stringHelper().joinStringArray(columns, ",") + ")";
        if (sourceTableName != null) {
            sql += AbstractStatement.SQL_COMPONENT_SEPARATOR + "TABLE " + sourceTableName;
        } else if (sourceSelectSQL != null) {
            sql += AbstractStatement.SQL_COMPONENT_SEPARATOR + sourceSelectSQL;
        } else {
            sql += AbstractStatement.SQL_COMPONENT_SEPARATOR + "VALUES" + AbstractStatement.SQL_COMPONENT_SEPARATOR;
            List<String> items = new ArrayList<>();
            for (List<String> row : batchValues) {
                items.add("(" + KeelHelpers.stringHelper().joinStringArray(row, ",") + ")");
            }
            sql += KeelHelpers.stringHelper().joinStringArray(items, "," + AbstractStatement.SQL_COMPONENT_SEPARATOR);
        }
        if (!onDuplicateKeyUpdateAssignmentMap.isEmpty()) {
            sql += AbstractStatement.SQL_COMPONENT_SEPARATOR + "ON DUPLICATE KEY UPDATE" + AbstractStatement.SQL_COMPONENT_SEPARATOR;
            List<String> items = new ArrayList<>();
            onDuplicateKeyUpdateAssignmentMap.forEach((key, value) -> items.add(key + " = " + value));
            sql += KeelHelpers.stringHelper().joinStringArray(items, "," + AbstractStatement.SQL_COMPONENT_SEPARATOR);
        }
        if (!getRemarkAsComment().isEmpty()) {
            sql += "\n-- " + getRemarkAsComment() + "\n";
        }
        return sql;
    }

    /**
     * @param sqlConnection get from pool
     * @return future with last inserted id; if any error occurs, failed future returned instead.
     * @since 1.7
     * @since 1.10, removed the recover block
     */
    public Future<Long> executeForLastInsertedID(SqlConnection sqlConnection) {
        return execute(sqlConnection)
                .compose(resultMatrix -> Future.succeededFuture(resultMatrix.getLastInsertedID()));
    }

    /**
     * @param namedMySQLConnection
     * @return
     * @since 3.0.11
     * @since 3.0.18 Finished Technical Preview.
     */
    public Future<Long> executeForLastInsertedID(NamedMySQLConnection namedMySQLConnection) {
        return executeForLastInsertedID(namedMySQLConnection.getSqlConnection());
    }

    /**
     * 按照最大块尺寸分裂！
     *
     * @param chunkSize an integer
     * @return a list of WriteIntoStatement
     * @since 2.3
     */
    public List<WriteIntoStatement> divide(int chunkSize) {
        if (sourceTableName != null || sourceSelectSQL != null) {
            return List.of(this);
        }

        List<WriteIntoStatement> list = new ArrayList<>();
        int size = this.batchValues.size();
        for (int chunkStartIndex = 0; chunkStartIndex < size; chunkStartIndex += chunkSize) {
            WriteIntoStatement chunkWIS = new WriteIntoStatement(this.writeType);

            chunkWIS.columns.addAll(this.columns);
            chunkWIS.onDuplicateKeyUpdateAssignmentMap.putAll(this.onDuplicateKeyUpdateAssignmentMap);
            chunkWIS.ignoreMark = this.ignoreMark;
            chunkWIS.schema = this.schema;
            chunkWIS.table = this.table;
            chunkWIS.batchValues.addAll(this.batchValues.subList(chunkStartIndex, Math.min(size, chunkStartIndex + chunkSize)));

            list.add(chunkWIS);
        }
        return list;
    }

    public static class RowToWrite {
        final Map<String, String> map = new ConcurrentHashMap<>();

        @Deprecated(since = "3.0.0", forRemoval = true)
        public RowToWrite putValue(String columnName, String value) {
            return put(columnName, value);
        }

        @Deprecated(since = "3.0.0", forRemoval = true)
        public RowToWrite putValue(String columnName, Number value) {
            return put(columnName, value);
        }

        /**
         * @since 3.0.1
         */
        public RowToWrite putNow(String columnName) {
            return this.putExpression(columnName, "now()");
        }

        public RowToWrite putExpression(String columnName, String expression) {
            map.put(columnName, expression);
            return this;
        }

        public RowToWrite put(String columnName, String value) {
            return putExpression(columnName, new Quoter(value).toString());
        }

        public RowToWrite put(String columnName, Number value) {
            if (value == null) return this.putExpression(columnName, "NULL");
            return putExpression(columnName, String.valueOf(value));
        }
    }
}
