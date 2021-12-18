package io.github.sinri.Keel.mysql.SQLBuilder;

import io.github.sinri.Keel.core.KeelHelper;
import io.github.sinri.Keel.mysql.condition.*;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class KeelMySQLDeleteSQLBuilder {
    /**
     * DELETE [LOW_PRIORITY] [QUICK] [IGNORE] FROM tbl_name [[AS] tbl_alias]
     * [PARTITION (partition_name [, partition_name] ...)]
     * [WHERE where_condition]
     * [ORDER BY ...]
     * [LIMIT row_count]
     */

    String schema;
    String table;
    List<KeelMySQLCondition> whereConditions = new ArrayList<>();
    List<String> sortRules = new ArrayList<>();
    long limit = 0;

    public KeelMySQLDeleteSQLBuilder() {

    }

    public KeelMySQLDeleteSQLBuilder from(String table) {
        this.schema = null;
        this.table = table;
        return this;
    }

    public KeelMySQLDeleteSQLBuilder from(String schema, String table) {
        this.schema = schema;
        this.table = table;
        return this;
    }

    public KeelMySQLDeleteSQLBuilder where(KeelMySQLCondition condition) {
        whereConditions.add(condition);
        return this;
    }

    public KeelMySQLDeleteSQLBuilder whereForRaw(Function<RawCondition, RawCondition> f) {
        RawCondition condition = new RawCondition();
        whereConditions.add(f.apply(condition));
        return this;
    }

    public KeelMySQLDeleteSQLBuilder whereForCompare(Function<CompareCondition, CompareCondition> f) {
        CompareCondition condition = new CompareCondition();
        whereConditions.add(f.apply(condition));
        return this;
    }

    public KeelMySQLDeleteSQLBuilder whereForAmongst(Function<AmongstCondition, AmongstCondition> f) {
        whereConditions.add(f.apply(new AmongstCondition()));
        return this;
    }

    public KeelMySQLDeleteSQLBuilder whereForAndGroup(Function<GroupCondition, GroupCondition> f) {
        GroupCondition condition = new GroupCondition(GroupCondition.JUNCTION_FOR_AND);
        whereConditions.add(f.apply(condition));
        return this;
    }

    public KeelMySQLDeleteSQLBuilder whereForOrGroup(Function<GroupCondition, GroupCondition> f) {
        GroupCondition condition = new GroupCondition(GroupCondition.JUNCTION_FOR_OR);
        whereConditions.add(f.apply(condition));
        return this;
    }

    public KeelMySQLDeleteSQLBuilder orderByAsc(String x) {
        sortRules.add(x);
        return this;
    }

    public KeelMySQLDeleteSQLBuilder orderByDesc(String x) {
        sortRules.add(x + " DESC");
        return this;
    }

    public KeelMySQLDeleteSQLBuilder limit(long limit) {
        this.limit = limit;
        return this;
    }

    public String toString() {
        String sql = "DELETE FROM ";
        if (schema != null) {
            sql += schema + ".";
        }
        sql += table;
        if (!whereConditions.isEmpty()) {
            sql += "\nWHERE ";
            sql += KeelHelper.joinStringArray(whereConditions, " and ");
        }
        if (!sortRules.isEmpty()) {
            sql += "\nORDER BY " + KeelHelper.joinStringArray(sortRules, ",");
        }
        if (limit > 0) {
            sql += "\nlimit " + limit;
        }
        return sql;
    }
}
