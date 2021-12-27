package io.github.sinri.keel.mysql.statement;

import io.github.sinri.keel.core.KeelHelper;
import io.github.sinri.keel.mysql.KeelMySQLQuoter;
import io.github.sinri.keel.mysql.condition.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class UpdateStatement {
    /**
     * UPDATE [LOW_PRIORITY] [IGNORE] table_reference
     * SET assignment_list
     * [WHERE where_condition]
     * [ORDER BY ...]
     * [LIMIT row_count]
     */

    String ignoreMark = "";
    String schema;
    String table;
    final List<String> assignments = new ArrayList<>();
    final List<KeelMySQLCondition> whereConditions = new ArrayList<>();
    final List<String> sortRules = new ArrayList<>();
    long limit = 0;

    public UpdateStatement() {

    }

    public UpdateStatement ignore() {
        this.ignoreMark = "IGNORE";
        return this;
    }

    public UpdateStatement table(String table) {
        this.schema = null;
        this.table = table;
        return this;
    }

    public UpdateStatement table(String schema, String table) {
        this.schema = schema;
        this.table = table;
        return this;
    }

    public UpdateStatement setWithExpression(Map<String, String> columnExpressionMapping) {
        columnExpressionMapping.forEach((k, v) -> assignments.add(k + "=" + v));
        return this;
    }

    public UpdateStatement setWithExpression(String column, String expression) {
        assignments.add(column + "=" + expression);
        return this;
    }

    public UpdateStatement setWithValue(String column, Number value) {
        assignments.add(column + "=" + (new KeelMySQLQuoter(value)));
        return this;
    }

    public UpdateStatement setWithValue(String column, String value) {
        assignments.add(column + "=" + (new KeelMySQLQuoter(value)));
        return this;
    }

    public UpdateStatement where(KeelMySQLCondition condition) {
        whereConditions.add(condition);
        return this;
    }

    public UpdateStatement whereForRaw(Function<RawCondition, RawCondition> f) {
        RawCondition condition = new RawCondition();
        whereConditions.add(f.apply(condition));
        return this;
    }

    public UpdateStatement whereForCompare(Function<CompareCondition, CompareCondition> f) {
        CompareCondition condition = new CompareCondition();
        whereConditions.add(f.apply(condition));
        return this;
    }

    public UpdateStatement whereForAmongst(Function<AmongstCondition, AmongstCondition> f) {
        whereConditions.add(f.apply(new AmongstCondition()));
        return this;
    }

    public UpdateStatement whereForAndGroup(Function<GroupCondition, GroupCondition> f) {
        GroupCondition condition = new GroupCondition(GroupCondition.JUNCTION_FOR_AND);
        whereConditions.add(f.apply(condition));
        return this;
    }

    public UpdateStatement whereForOrGroup(Function<GroupCondition, GroupCondition> f) {
        GroupCondition condition = new GroupCondition(GroupCondition.JUNCTION_FOR_OR);
        whereConditions.add(f.apply(condition));
        return this;
    }

    public UpdateStatement orderByAsc(String x) {
        sortRules.add(x);
        return this;
    }

    public UpdateStatement orderByDesc(String x) {
        sortRules.add(x + " DESC");
        return this;
    }

    public UpdateStatement limit(long limit) {
        this.limit = limit;
        return this;
    }

    public String toString() {
        String sql = "UPDATE " + ignoreMark;
        if (schema != null) {
            sql += " " + schema + ".";
        }
        sql += table;
        sql += "\nSET " + KeelHelper.joinStringArray(assignments, ",");
        if (!whereConditions.isEmpty()) {
            sql += "\nWHERE " + KeelHelper.joinStringArray(whereConditions, " and ");
        }
        if (!sortRules.isEmpty()) {
            sql += "\nORDER BY " + KeelHelper.joinStringArray(sortRules, ",");
        }
        if (limit > 0) {
            sql += "\nLIMIT " + limit;
        }
        return sql;
    }
}
