package io.github.sinri.keel.mysql.statement;


import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import static io.github.sinri.keel.helper.KeelHelpersInterface.KeelHelpers;

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

    @Nullable String schema;
    @Nonnull String table="NOT-SET";
    long limit = 0;

    public DeleteStatement() {

    }

    public DeleteStatement from(@Nonnull String table) {
        this.schema = null;
        this.table = table;
        return this;
    }

    public DeleteStatement from(@Nullable String schema, @Nonnull String table) {
        this.schema = schema;
        this.table = table;
        return this;
    }

    /**
     * @param function ConditionsComponent → this
     * @return this
     * @since 1.4
     */
    public DeleteStatement where(@Nonnull Function<ConditionsComponent, ConditionsComponent> function) {
        function.apply(whereConditionsComponent);
        return this;
    }

    public DeleteStatement orderByAsc(@Nonnull String x) {
        sortRules.add(x);
        return this;
    }

    public DeleteStatement orderByDesc(@Nonnull String x) {
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
            sql += AbstractStatement.SQL_COMPONENT_SEPARATOR + "ORDER BY " + KeelHelpers.stringHelper().joinStringArray(sortRules, ",");
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
