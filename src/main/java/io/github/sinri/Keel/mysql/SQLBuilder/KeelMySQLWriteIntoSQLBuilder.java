package io.github.sinri.Keel.mysql.SQLBuilder;

import io.github.sinri.Keel.core.KeelHelper;
import io.github.sinri.Keel.mysql.KeelMySQLQuoter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class KeelMySQLWriteIntoSQLBuilder {
    /**
     * insert [ignore] into schema.table (column...) values (value...),... ON DUPLICATE KEY UPDATE assignment_list
     * insert [ignore] into schema.table (column...) [select ...| table ...] ON DUPLICATE KEY UPDATE assignment_list
     */

    public static final String INSERT = "INSERT";
    public static final String REPLACE = "REPLACE";

    String writeType = INSERT;
    String ignoreMark = "";
    String schema;
    String table;
    List<String> columns = new ArrayList<>();
    List<List<String>> batchValues = new ArrayList<>();
    String sourceSelectSQL;
    String sourceTableName;
    Map<String, String> onDuplicateKeyUpdateAssignmentMap = new HashMap<>();

    public KeelMySQLWriteIntoSQLBuilder() {

    }

    public KeelMySQLWriteIntoSQLBuilder(String writeType) {
        this.writeType = writeType;
    }

    public KeelMySQLWriteIntoSQLBuilder intoTable(String table) {
        this.table = table;
        return this;
    }

    public KeelMySQLWriteIntoSQLBuilder intoTable(String schema, String table) {
        this.schema = schema;
        this.table = table;
        return this;
    }

    public KeelMySQLWriteIntoSQLBuilder ignore() {
        this.ignoreMark = "IGNORE";
        return this;
    }

    public KeelMySQLWriteIntoSQLBuilder columns(List<String> columns) {
        this.columns.addAll(columns);
        return this;
    }

    public KeelMySQLWriteIntoSQLBuilder addDataMatrix(List<List<Object>> batch) {
        for (var row : batch) {
            List<String> t = new ArrayList<>();
            for (var item : row) {
                t.add(new KeelMySQLQuoter(item).toString());
            }
            this.batchValues.add(t);
        }
        return this;
    }

    public KeelMySQLWriteIntoSQLBuilder addDataRow(List<Object> row) {
        List<String> t = new ArrayList<>();
        for (var item : row) {
            t.add(new KeelMySQLQuoter(item).toString());
        }
        this.batchValues.add(t);
        return this;
    }

    public KeelMySQLWriteIntoSQLBuilder fromSelection(String selectionSQL) {
        this.sourceSelectSQL = selectionSQL;
        return this;
    }

    public KeelMySQLWriteIntoSQLBuilder fromTable(String tableName) {
        this.sourceTableName = tableName;
        return this;
    }

    public KeelMySQLWriteIntoSQLBuilder onDuplicateKeyUpdate(String column, String updateExpression) {
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
            sql += "\nTABLE " + sourceTableName;
        } else if (sourceSelectSQL != null) {
            sql += "\n" + sourceSelectSQL;
        } else {
            sql += "\nVALUES\n";
            List<String> items = new ArrayList<>();
            for (var row : batchValues) {
                items.add("(" + KeelHelper.joinStringArray(row, ",") + ")");
            }
            sql += KeelHelper.joinStringArray(items, ",\n");
        }
        if (!onDuplicateKeyUpdateAssignmentMap.isEmpty()) {
            sql += "\nON DUPLICATE KEY UPDATE\n";
            List<String> items = new ArrayList<>();
            onDuplicateKeyUpdateAssignmentMap.forEach((key, value) -> items.add(key + " = " + value));
            sql += KeelHelper.joinStringArray(items, ",\n");
        }
        return sql;
    }
}
