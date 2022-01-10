package io.github.sinri.keel.mysql.statement;

import io.github.sinri.keel.Keel;
import io.github.sinri.keel.core.KeelHelper;
import io.vertx.core.Future;
import io.vertx.sqlclient.SqlConnection;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class DeleteStatement extends AbstractStatement {
    /**
     * DELETE [LOW_PRIORITY] [QUICK] [IGNORE] FROM tbl_name [[AS] tbl_alias]
     * [PARTITION (partition_name [, partition_name] ...)]
     * [WHERE where_condition]
     * [ORDER BY ...]
     * [LIMIT row_count]
     */

    String schema;
    String table;

    // final List<KeelMySQLCondition> whereConditions = new ArrayList<>();
    final ConditionsComponent whereConditionsComponent = new ConditionsComponent();

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

    /**
     * @param function
     * @return
     * @since 1.4
     */
    public DeleteStatement where(Function<ConditionsComponent, ConditionsComponent> function) {
        function.apply(whereConditionsComponent);
        return this;
    }

//    public DeleteStatement where(KeelMySQLCondition condition) {
//        whereConditions.add(condition);
//        return this;
//    }
//
//    public DeleteStatement whereForRaw(Function<RawCondition, RawCondition> f) {
//        RawCondition condition = new RawCondition();
//        whereConditions.add(f.apply(condition));
//        return this;
//    }
//
//    public DeleteStatement whereForCompare(Function<CompareCondition, CompareCondition> f) {
//        CompareCondition condition = new CompareCondition();
//        whereConditions.add(f.apply(condition));
//        return this;
//    }
//
//    public DeleteStatement whereForAmongst(Function<AmongstCondition, AmongstCondition> f) {
//        whereConditions.add(f.apply(new AmongstCondition()));
//        return this;
//    }
//
//    public DeleteStatement whereForAndGroup(Function<GroupCondition, GroupCondition> f) {
//        GroupCondition condition = new GroupCondition(GroupCondition.JUNCTION_FOR_AND);
//        whereConditions.add(f.apply(condition));
//        return this;
//    }
//
//    public DeleteStatement whereForOrGroup(Function<GroupCondition, GroupCondition> f) {
//        GroupCondition condition = new GroupCondition(GroupCondition.JUNCTION_FOR_OR);
//        whereConditions.add(f.apply(condition));
//        return this;
//    }

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
//        if (!whereConditions.isEmpty()) {
//            sql += "\nWHERE ";
//            sql += KeelHelper.joinStringArray(whereConditions, " and ");
//        }
        if (!whereConditionsComponent.isEmpty()) {
            sql += "\n" + whereConditionsComponent;
        }
        if (!sortRules.isEmpty()) {
            sql += "\nORDER BY " + KeelHelper.joinStringArray(sortRules, ",");
        }
        if (limit > 0) {
            sql += "\nlimit " + limit;
        }
        return sql;
    }

    /**
     * @param sqlConnection get from pool
     * @return future with affected rows; -1 when failed
     * @since 1.7
     */
    public Future<Integer> executeForAffectedRows(SqlConnection sqlConnection) {
        return execute(sqlConnection)
                .compose(resultMatrix -> Future.succeededFuture(resultMatrix.getTotalAffectedRows()))
                .recover(throwable -> {
                    Keel.outputLogger("MySQL").warning(getClass().getName() + " executeForAffectedRows failed [" + throwable.getMessage() + "] when executing SQL: " + this);
                    return Future.succeededFuture(-1);
                });
    }
}
