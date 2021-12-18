package io.github.sinri.Keel.mysql.SQLBuilder;

import io.github.sinri.Keel.core.KeelHelper;
import io.github.sinri.Keel.mysql.KeelMySQLQuoter;
import io.github.sinri.Keel.mysql.condition.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class KeelMySQLUpdateSQLBuilder {
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
    List<String> assignments = new ArrayList<>();
    List<KeelMySQLCondition> whereConditions = new ArrayList<>();
    List<String> sortRules = new ArrayList<>();
    long limit = 0;

    public KeelMySQLUpdateSQLBuilder() {

    }

    public KeelMySQLUpdateSQLBuilder ignore() {
        this.ignoreMark = "IGNORE";
        return this;
    }

    public KeelMySQLUpdateSQLBuilder table(String table) {
        this.schema = null;
        this.table = table;
        return this;
    }

    public KeelMySQLUpdateSQLBuilder table(String schema, String table) {
        this.schema = schema;
        this.table = table;
        return this;
    }

    public KeelMySQLUpdateSQLBuilder setWithExpression(Map<String, String> columnExpressionMapping) {
        columnExpressionMapping.forEach((k, v) -> {
            assignments.add(k + "=" + v);
        });
        return this;
    }

    public KeelMySQLUpdateSQLBuilder setWithExpression(String column, String expression) {
        assignments.add(column + "=" + expression);
        return this;
    }

    public KeelMySQLUpdateSQLBuilder setWithValue(String column, Object value) {
        assignments.add(column + "=" + (new KeelMySQLQuoter(value)));
        return this;
    }

    public KeelMySQLUpdateSQLBuilder where(KeelMySQLCondition condition) {
        whereConditions.add(condition);
        return this;
    }

    public KeelMySQLUpdateSQLBuilder whereForRaw(Function<RawCondition, RawCondition> f) {
        RawCondition condition = new RawCondition();
        whereConditions.add(f.apply(condition));
        return this;
    }

    public KeelMySQLUpdateSQLBuilder whereForCompare(Function<CompareCondition, CompareCondition> f) {
        CompareCondition condition = new CompareCondition();
        whereConditions.add(f.apply(condition));
        return this;
    }

    public KeelMySQLUpdateSQLBuilder whereForAmongst(Function<AmongstCondition, AmongstCondition> f) {
        whereConditions.add(f.apply(new AmongstCondition()));
        return this;
    }

    public KeelMySQLUpdateSQLBuilder whereForAndGroup(Function<GroupCondition, GroupCondition> f) {
        GroupCondition condition = new GroupCondition(GroupCondition.JUNCTION_FOR_AND);
        whereConditions.add(f.apply(condition));
        return this;
    }

    public KeelMySQLUpdateSQLBuilder whereForOrGroup(Function<GroupCondition, GroupCondition> f) {
        GroupCondition condition = new GroupCondition(GroupCondition.JUNCTION_FOR_OR);
        whereConditions.add(f.apply(condition));
        return this;
    }

    public KeelMySQLUpdateSQLBuilder orderByAsc(String x) {
        sortRules.add(x);
        return this;
    }

    public KeelMySQLUpdateSQLBuilder orderByDesc(String x) {
        sortRules.add(x + " DESC");
        return this;
    }

    public KeelMySQLUpdateSQLBuilder limit(long limit) {
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
