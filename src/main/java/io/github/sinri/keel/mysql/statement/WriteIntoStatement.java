package io.github.sinri.keel.mysql.statement;

import io.github.sinri.keel.core.KeelHelper;
import io.github.sinri.keel.mysql.KeelMySQLQuoter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WriteIntoStatement {
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
    final List<String> columns = new ArrayList<>();
    final List<List<String>> batchValues = new ArrayList<>();
    String sourceSelectSQL;
    String sourceTableName;
    final Map<String, String> onDuplicateKeyUpdateAssignmentMap = new HashMap<>();

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
            sql += "\nTABLE " + sourceTableName;
        } else if (sourceSelectSQL != null) {
            sql += "\n" + sourceSelectSQL;
        } else {
            sql += "\nVALUES\n";
            List<String> items = new ArrayList<>();
            for (List<String> row : batchValues) {
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
