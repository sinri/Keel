package io.github.sinri.keel.mysql.statement;

import io.github.sinri.keel.mysql.NamedMySQLConnection;
import io.github.sinri.keel.mysql.Quoter;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.sqlclient.SqlConnection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static io.github.sinri.keel.helper.KeelHelpersInterface.KeelHelpers;

public class WriteIntoStatement extends AbstractModifyStatement {
    /**
     * insert [ignore] into schema.table (column...) values (value...),... ON DUPLICATE KEY UPDATE assignment_list
     * insert [ignore] into schema.table (column...) [select ...| table ...] ON DUPLICATE KEY UPDATE assignment_list
     */

    public static final String INSERT = "INSERT";
    public static final String REPLACE = "REPLACE";
    @NotNull
    final List<String> columns = new ArrayList<>();
    @NotNull
    final List<List<String>> batchValues = new ArrayList<>();
    @NotNull
    final Map<String, String> onDuplicateKeyUpdateAssignmentMap = new HashMap<>();
    @NotNull
    final String writeType;
    @NotNull
    String ignoreMark = "";
    @Nullable
    String schema;
    @NotNull
    String table = "TABLE-NOT-SET";
    @Nullable
    String sourceSelectSQL;
    @Nullable
    String sourceTableName;

    public WriteIntoStatement() {
        this.writeType = INSERT;
    }

    public WriteIntoStatement(@NotNull String writeType) {
        this.writeType = writeType;
    }

    public WriteIntoStatement intoTable(@NotNull String table) {
        if (table.isBlank()) throw new IllegalArgumentException("Table is blank");
        this.table = table;
        return this;
    }

    public WriteIntoStatement intoTable(@Nullable String schema, @NotNull String table) {
        this.schema = schema;
        this.table = table;
        return this;
    }

    public WriteIntoStatement ignore() {
        this.ignoreMark = "IGNORE";
        return this;
    }

    public WriteIntoStatement columns(@NotNull List<String> columns) {
        this.columns.addAll(columns);
        return this;
    }

    public WriteIntoStatement addDataMatrix(@NotNull List<List<Object>> batch) {
        for (List<Object> row : batch) {
            this.addDataRow(row);
        }
        return this;
    }

    public WriteIntoStatement addDataRow(@NotNull List<Object> row) {
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
    public WriteIntoStatement macroWriteRows(@NotNull Collection<RowToWrite> rows) {
        if (rows.isEmpty()) {
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
    public WriteIntoStatement macroWriteOneRow(@NotNull RowToWrite row) {
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
    public WriteIntoStatement macroWriteOneRow(@NotNull Handler<RowToWrite> rowEditor) {
        RowToWrite rowToWrite = new RowToWrite();
        rowEditor.handle(rowToWrite);
        return macroWriteOneRow(rowToWrite);
    }

    public WriteIntoStatement fromSelection(@NotNull String selectionSQL) {
        this.sourceSelectSQL = selectionSQL;
        return this;
    }

    public WriteIntoStatement fromTable(@NotNull String tableName) {
        this.sourceTableName = tableName;
        return this;
    }

    public WriteIntoStatement onDuplicateKeyUpdate(@NotNull String column, @NotNull String updateExpression) {
        this.onDuplicateKeyUpdateAssignmentMap.put(column, updateExpression);
        return this;
    }

    /**
     * @param fieldName the raw column name
     * @return as `onDuplicateKeyUpdate` does
     * @since 1.10
     */
    public WriteIntoStatement onDuplicateKeyUpdateField(@NotNull String fieldName) {
        return this.onDuplicateKeyUpdate(fieldName, "values(" + fieldName + ")");
    }

    /**
     * @param fieldNameList the raw column name list
     * @return as `onDuplicateKeyUpdate` does
     * @since 1.10
     */
    public WriteIntoStatement onDuplicateKeyUpdateFields(@NotNull List<String> fieldNameList) {
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
    public WriteIntoStatement onDuplicateKeyUpdateExceptField(@NotNull String fieldName) {
        if (columns.isEmpty()) {
            throw new RuntimeException("Columns not set yet");
        }
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
    public WriteIntoStatement onDuplicateKeyUpdateExceptFields(@NotNull List<String> fieldNameList) {
        if (columns.isEmpty()) {
            throw new RuntimeException("Columns not set yet");
        }
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
    public Future<Long> executeForLastInsertedID(@NotNull SqlConnection sqlConnection) {
        return execute(sqlConnection)
                .compose(resultMatrix -> Future.succeededFuture(resultMatrix.getLastInsertedID()));
    }

    /**
     * @since 3.0.11
     * @since 3.0.18 Finished Technical Preview.
     */
    public Future<Long> executeForLastInsertedID(@NotNull NamedMySQLConnection namedMySQLConnection) {
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

        /**
         * @param jsonObject One row as a JsonObject
         * @since 3.1.2
         */
        public static RowToWrite fromJsonObject(@NotNull JsonObject jsonObject) {
            RowToWrite rowToWrite = new RowToWrite();
            jsonObject.forEach(entry -> {
                rowToWrite.put(entry.getKey(), entry.getValue());
            });
            return rowToWrite;
        }

        /**
         * @param jsonArray Rows in a JsonArray; each item of the array should be a JsonObject to be a row.
         * @since 3.1.2
         */
        public static Collection<RowToWrite> fromJsonObjectArray(@NotNull JsonArray jsonArray) {
            Collection<RowToWrite> rows = new ArrayList<>();
            jsonArray.forEach(item -> {
                Objects.requireNonNull(item);
                if (item instanceof JsonObject o) {
                    rows.add(fromJsonObject(o));
                } else {
                    throw new IllegalArgumentException("JsonArray contains non JsonObject item.");
                }
            });
            return rows;
        }

        /**
         * @param jsonObjects each item to be a row.
         * @since 3.1.2
         */
        public static Collection<RowToWrite> fromJsonObjectArray(@NotNull List<JsonObject> jsonObjects) {
            Collection<RowToWrite> rows = new ArrayList<>();
            jsonObjects.forEach(item -> {
                Objects.requireNonNull(item);
                rows.add(fromJsonObject(item));
            });
            return rows;
        }

        /**
         * @since 3.0.1
         */
        public RowToWrite putNow(@NotNull String columnName) {
            return this.putExpression(columnName, "now()");
        }

        public RowToWrite putExpression(@NotNull String columnName, @NotNull String expression) {
            map.put(columnName, expression);
            return this;
        }

        /**
         * @since 3.1.0
         */
        public RowToWrite put(@NotNull String columnName, @Nullable Object value) {
            if (value == null) return this.putExpression(columnName, "NULL");
            else if (value instanceof Number) {
                return putExpression(columnName, String.valueOf(value));
            } else {
                return putExpression(columnName, new Quoter(value.toString()).toString());
            }
        }
    }
}
