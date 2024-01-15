package io.github.sinri.keel.mysql.statement;

import io.github.sinri.keel.mysql.condition.CompareCondition;
import io.github.sinri.keel.mysql.condition.GroupCondition;
import io.github.sinri.keel.mysql.condition.MySQLCondition;
import io.github.sinri.keel.mysql.condition.RawCondition;
import io.github.sinri.keel.mysql.exception.KeelSQLGenerateError;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

import static io.github.sinri.keel.helper.KeelHelpersInterface.KeelHelpers;

public class SelectStatement extends AbstractReadStatement {
    //    private final List<KeelMySQLCondition> whereConditions = new ArrayList<>();
    final ConditionsComponent whereConditionsComponent = new ConditionsComponent();
    //    private final List<KeelMySQLCondition> havingConditions = new ArrayList<>();
    final ConditionsComponent havingConditionsComponent = new ConditionsComponent();
    private final List<String> tables = new ArrayList<>();
    private final List<String> columns = new ArrayList<>();
    private final List<String> categories = new ArrayList<>();
    private final List<String> sortRules = new ArrayList<>();
    private long offset = 0;
    private long limit = 0;
    private @Nonnull String lockMode = "";
    /**
     * For MySQL 5.7 ,8.0 or higher, in Select, to limit the max execution time in millisecond.
     *
     * @since 3.1.0
     */
    private @Nullable Long maxExecutionTime = null;

    public SelectStatement from(@Nonnull String tableExpression) {
        return from(tableExpression, null);
    }

    public SelectStatement from(@Nonnull String tableExpression, @Nullable String alias) {
        if (tableExpression.isBlank()) {
            throw new KeelSQLGenerateError("Select from blank");
        }
        String x = tableExpression;
        if (alias != null) {
            x += " AS " + alias;
        }
        if (tables.isEmpty()) {
            tables.add(x);
        } else {
            tables.set(0, x);
        }
        return this;
    }

    /**
     * @since 2.8
     */
    public SelectStatement from(@Nonnull AbstractReadStatement subQuery,@Nonnull String alias) {
        if (alias.isBlank()) {
            throw new KeelSQLGenerateError("Sub Query without alias");
        }
        return this.from("(" + subQuery.toString() + ")", alias);
    }

    public SelectStatement leftJoin(@Nonnull Function<JoinComponent, JoinComponent> joinFunction) {
        JoinComponent join = new JoinComponent("LEFT JOIN");
        tables.add(joinFunction.apply(join).toString());
        return this;
    }

    public SelectStatement rightJoin(@Nonnull Function<JoinComponent, JoinComponent> joinFunction) {
        JoinComponent join = new JoinComponent("RIGHT JOIN");
        tables.add(joinFunction.apply(join).toString());
        return this;
    }

    public SelectStatement innerJoin(@Nonnull Function<JoinComponent, JoinComponent> joinFunction) {
        JoinComponent join = new JoinComponent("INNER JOIN");
        tables.add(joinFunction.apply(join).toString());
        return this;
    }

    public SelectStatement straightJoin(@Nonnull Function<JoinComponent, JoinComponent> joinFunction) {
        JoinComponent join = new JoinComponent("STRAIGHT_JOIN");
        tables.add(joinFunction.apply(join).toString());
        return this;
    }

    public SelectStatement column(@Nonnull Function<ColumnComponent, ColumnComponent> func) {
        columns.add(func.apply(new ColumnComponent()).toString());
        return this;
    }

    public SelectStatement columnWithAlias(@Nonnull String columnExpression, @Nonnull String alias) {
        if(columnExpression.isBlank() || alias.isBlank()){
            throw new IllegalArgumentException("Column or its alias is empty.");
        }
        columns.add(columnExpression + " as `" + alias + "`");
        return this;
    }

    public SelectStatement columnAsExpression(@Nonnull String fieldName) {
        columns.add(fieldName);
        return this;
    }

    /**
     * @param function ConditionsComponent → ConditionsComponent it self
     * @return this
     * @since 1.4
     */
    public SelectStatement where(@Nonnull Function<ConditionsComponent, ConditionsComponent> function) {
        function.apply(whereConditionsComponent);
        return this;
    }

    public SelectStatement groupBy(@Nonnull String x) {
        categories.add(x);
        return this;
    }

    public SelectStatement groupBy(@Nonnull List<String> x) {
        categories.addAll(x);
        return this;
    }

    public SelectStatement having(@Nonnull Function<ConditionsComponent, ConditionsComponent> function) {
        function.apply(havingConditionsComponent);
        return this;
    }

    public SelectStatement orderByAsc(@Nonnull String x) {
        sortRules.add(x);
        return this;
    }

    public SelectStatement orderByDesc(@Nonnull String x) {
        sortRules.add(x + " DESC");
        return this;
    }

    public SelectStatement limit(long limit) {
        this.offset = 0;
        this.limit = limit;
        return this;
    }

    public SelectStatement limit(long limit, long offset) {
        this.offset = offset;
        this.limit = limit;
        return this;
    }

    public SelectStatement setLockMode(@Nonnull String lockMode) {
        Objects.requireNonNull(lockMode);
        this.lockMode = lockMode;
        return this;
    }

    /**
     * Available in MySQL 5.7, 8.0 or higher.
     *
     * @since 3.1.0
     */
    public SelectStatement setMaxExecutionTime(long maxExecutionTime) {
        this.maxExecutionTime = maxExecutionTime;
        return this;
    }

    public String toString() {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT ");

        // since 3.1.0: The Max Statement Execution Time
        //  MYSQL 5.7, 8.0+ /*+ MAX_EXECUTION_TIME(1000) */
        //  MYSQL 5.6 /*+ MAX_STATEMENT_TIME(1000) */ THIS IS NOT FOR ONE STATEMENT.
        if (this.maxExecutionTime != null) {
            sql.append("/*+ MAX_EXECUTION_TIME(").append(maxExecutionTime).append(") */ ")
                    .append(AbstractStatement.SQL_COMPONENT_SEPARATOR);
        }

        if (columns.isEmpty()) {
            sql.append("*");
        } else {
            sql.append(KeelHelpers.stringHelper().joinStringArray(columns, ","));
        }
        if (!tables.isEmpty()) {
            sql.append(AbstractStatement.SQL_COMPONENT_SEPARATOR).append("FROM ").append(KeelHelpers.stringHelper().joinStringArray(tables, AbstractStatement.SQL_COMPONENT_SEPARATOR));
        }
        if (!whereConditionsComponent.isEmpty()) {
            sql.append(AbstractStatement.SQL_COMPONENT_SEPARATOR).append("WHERE ").append(whereConditionsComponent);
        }
        if (!categories.isEmpty()) {
            sql.append(AbstractStatement.SQL_COMPONENT_SEPARATOR).append("GROUP BY ").append(KeelHelpers.stringHelper().joinStringArray(categories, ","));
        }
        if (!havingConditionsComponent.isEmpty()) {
            sql.append(AbstractStatement.SQL_COMPONENT_SEPARATOR).append("HAVING ").append(havingConditionsComponent);
        }
        if (!sortRules.isEmpty()) {
            sql.append(AbstractStatement.SQL_COMPONENT_SEPARATOR).append("ORDER BY ").append(KeelHelpers.stringHelper().joinStringArray(sortRules, ","));
        }
        if (limit > 0) {
            sql.append(AbstractStatement.SQL_COMPONENT_SEPARATOR).append("LIMIT ").append(limit).append(" OFFSET ").append(offset);
        }
        if (!"".equals(lockMode)) {
            sql.append(AbstractStatement.SQL_COMPONENT_SEPARATOR).append(lockMode);
        }
        if (!getRemarkAsComment().isEmpty()) {
            sql.append("\n-- ").append(getRemarkAsComment()).append("\n");
        }
        return String.valueOf(sql);
    }

    public static class JoinComponent {
        @Nonnull final String joinType;
        final List<MySQLCondition> onConditions = new ArrayList<>();
        @Nonnull String tableExpression="NOT-SET";
        @Nullable String alias;

        public JoinComponent(@Nonnull String joinType) {
            this.joinType = joinType;
        }

        public JoinComponent table(@Nonnull String tableExpression) {
            this.tableExpression = tableExpression;
            return this;
        }

        public JoinComponent alias(@Nonnull String alias) {
            this.alias = alias;
            return this;
        }

        public JoinComponent onForRaw(@Nonnull Function<RawCondition, RawCondition> func) {
            this.onConditions.add(func.apply(new RawCondition()));
            return this;
        }

        public JoinComponent onForAndGroup(@Nonnull Function<GroupCondition, GroupCondition> func) {
            this.onConditions.add(func.apply(new GroupCondition(GroupCondition.JUNCTION_FOR_AND)));
            return this;
        }

        public JoinComponent onForOrGroup(@Nonnull Function<GroupCondition, GroupCondition> func) {
            this.onConditions.add(func.apply(new GroupCondition(GroupCondition.JUNCTION_FOR_OR)));
            return this;
        }

        public JoinComponent onForCompare(@Nonnull Function<CompareCondition, CompareCondition> func) {
            this.onConditions.add(func.apply(new CompareCondition()));
            return this;
        }

        public String toString() {
            String s = joinType + " " + tableExpression;
            if (alias != null) {
                s += " AS " + alias;
            }
            if (!onConditions.isEmpty()) {
                s += " ON ";
                s += KeelHelpers.stringHelper().joinStringArray(onConditions, " AND ");
            }
            return s;
        }
    }

    public static class ColumnComponent {
        @Nullable String schema;
        @Nullable String field="NOT-SET";
        @Nullable String expression;
        @Nullable String alias;

        public ColumnComponent field(@Nonnull String field) {
            this.field = field;
            return this;
        }

        public ColumnComponent field(@Nullable String schema,@Nonnull String field) {
            this.schema = schema;
            this.field = field;
            return this;
        }

        public ColumnComponent expression(@Nonnull String expression) {
            this.expression = expression;
            return this;
        }

        public ColumnComponent alias(@Nullable String alias) {
            this.alias = alias;
            return this;
        }

        public String toString() {
            StringBuilder column = new StringBuilder();
            if (expression == null) {
                if (schema == null) {
                    column.append("`").append(field).append("`");
                } else {
                    column.append("`").append(schema).append("`.`").append(field).append("`");
                }
            } else {
                column.append(expression);
            }

            if (alias != null) {
                column.append(" AS `").append(alias).append("`");
            }
            return String.valueOf(column);
        }
    }
}
