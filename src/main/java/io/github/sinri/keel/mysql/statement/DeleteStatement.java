package io.github.sinri.keel.mysql.statement;

import io.github.sinri.keel.core.KeelHelper;
import io.github.sinri.keel.mysql.condition.*;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class DeleteStatement {
    /**
     * DELETE [LOW_PRIORITY] [QUICK] [IGNORE] FROM tbl_name [[AS] tbl_alias]
     * [PARTITION (partition_name [, partition_name] ...)]
     * [WHERE where_condition]
     * [ORDER BY ...]
     * [LIMIT row_count]
     */

    String schema;
    String table;
    final List<KeelMySQLCondition> whereConditions = new ArrayList<>();
    final List<String> sortRules = new ArrayList<>();
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

    public DeleteStatement where(KeelMySQLCondition condition) {
        whereConditions.add(condition);
        return this;
    }

    public DeleteStatement whereForRaw(Function<RawCondition, RawCondition> f) {
        RawCondition condition = new RawCondition();
        whereConditions.add(f.apply(condition));
        return this;
    }

    public DeleteStatement whereForCompare(Function<CompareCondition, CompareCondition> f) {
        CompareCondition condition = new CompareCondition();
        whereConditions.add(f.apply(condition));
        return this;
    }

    public DeleteStatement whereForAmongst(Function<AmongstCondition, AmongstCondition> f) {
        whereConditions.add(f.apply(new AmongstCondition()));
        return this;
    }

    public DeleteStatement whereForAndGroup(Function<GroupCondition, GroupCondition> f) {
        GroupCondition condition = new GroupCondition(GroupCondition.JUNCTION_FOR_AND);
        whereConditions.add(f.apply(condition));
        return this;
    }

    public DeleteStatement whereForOrGroup(Function<GroupCondition, GroupCondition> f) {
        GroupCondition condition = new GroupCondition(GroupCondition.JUNCTION_FOR_OR);
        whereConditions.add(f.apply(condition));
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
