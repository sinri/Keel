package io.github.sinri.keel.mysql.statement;

import io.github.sinri.keel.Keel;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class DeleteStatement extends AbstractModifyStatement {
    // final List<KeelMySQLCondition> whereConditions = new ArrayList<>();
    final ConditionsComponent whereConditionsComponent = new ConditionsComponent();
    final List<String> sortRules = new ArrayList<>();
    /**
     * DELETE [LOW_PRIORITY] [QUICK] [IGNORE] FROM tbl_name [[AS] tbl_alias]
     * [PARTITION (partition_name [, partition_name] ...)]
     * [WHERE where_condition]
     * [ORDER BY ...]
     * [LIMIT row_count]
     */

    String schema;
    String table;
    long limit = 0;

    public DeleteStatement() {

    }

    public DeleteStatement from(String table) {
        this.schema = null;
        this.table = table;
        return this;
    }

    public DeleteStatement from(String schema, String table) {
        this.schema = schema;
        this.table = table;
        return this;
    }

    /**
     * @param function ConditionsComponent -> this
     * @return this
     * @since 1.4
     */
    public DeleteStatement where(Function<ConditionsComponent, ConditionsComponent> function) {
        function.apply(whereConditionsComponent);
        return this;
    }

    public DeleteStatement orderByAsc(String x) {
        sortRules.add(x);
        return this;
    }

    public DeleteStatement orderByDesc(String x) {
        sortRules.add(x + " DESC");
        return this;
    }

    public DeleteStatement limit(long limit) {
        this.limit = limit;
        return this;
    }

    public String toString() {
        String sql = "DELETE FROM ";
        if (schema != null) {
            sql += schema + ".";
        }
        sql += table;
        if (!whereConditionsComponent.isEmpty()) {
            sql += AbstractStatement.SQL_COMPONENT_SEPARATOR + "WHERE " + whereConditionsComponent;
        }
        if (!sortRules.isEmpty()) {
            sql += AbstractStatement.SQL_COMPONENT_SEPARATOR + "ORDER BY " + Keel.helpers().string().joinStringArray(sortRules, ",");
        }
        if (limit > 0) {
            sql += AbstractStatement.SQL_COMPONENT_SEPARATOR + "limit " + limit;
        }
        if (!getRemarkAsComment().isEmpty()) {
            sql += "\n-- " + getRemarkAsComment() + "\n";
        }
        return sql;
    }
}
