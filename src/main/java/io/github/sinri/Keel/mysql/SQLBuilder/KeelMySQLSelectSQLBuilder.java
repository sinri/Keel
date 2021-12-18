package io.github.sinri.Keel.mysql.SQLBuilder;

import io.github.sinri.Keel.core.KeelHelper;
import io.github.sinri.Keel.mysql.condition.*;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class KeelMySQLSelectSQLBuilder {
    private final List<String> tables = new ArrayList<>();
    private final List<String> columns = new ArrayList<>();
    private final List<KeelMySQLCondition> whereConditions = new ArrayList<>();
    private final List<String> categories = new ArrayList<>();
    private final List<KeelMySQLCondition> havingConditions = new ArrayList<>();
    private final List<String> sortRules = new ArrayList<>();
    private long offset = 0;
    private long limit = 0;
    private String lockMode = "";

    public KeelMySQLSelectSQLBuilder from(String tableExpression) {
        return from(tableExpression, null);
    }

    public KeelMySQLSelectSQLBuilder from(String tableExpression, String alias) {
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

    public KeelMySQLSelectSQLBuilder leftJoin(Function<JoinComponent, JoinComponent> joinFunction) {
        JoinComponent join = new JoinComponent("LEFT JOIN");
        tables.add(joinFunction.apply(join).toString());
        return this;
    }

    public KeelMySQLSelectSQLBuilder rightJoin(Function<JoinComponent, JoinComponent> joinFunction) {
        JoinComponent join = new JoinComponent("RIGHT JOIN");
        tables.add(joinFunction.apply(join).toString());
        return this;
    }

    public KeelMySQLSelectSQLBuilder innerJoin(Function<JoinComponent, JoinComponent> joinFunction) {
        JoinComponent join = new JoinComponent("INNER JOIN");
        tables.add(joinFunction.apply(join).toString());
        return this;
    }

    public KeelMySQLSelectSQLBuilder straightJoin(Function<JoinComponent, JoinComponent> joinFunction) {
        JoinComponent join = new JoinComponent("STRAIGHT_JOIN");
        tables.add(joinFunction.apply(join).toString());
        return this;
    }

    public KeelMySQLSelectSQLBuilder column(Function<ColumnComponent, ColumnComponent> func) {
        columns.add(func.apply(new ColumnComponent()).toString());
        return this;
    }

    public KeelMySQLSelectSQLBuilder columnWithAlias(String columnExpression, String alias) {
        columns.add(columnExpression + " as `" + alias + "`");
        return this;
    }

    public KeelMySQLSelectSQLBuilder columnAsExpression(String fieldName) {
        columns.add(fieldName);
        return this;
    }

    public KeelMySQLSelectSQLBuilder where(KeelMySQLCondition condition) {
        whereConditions.add(condition);
        return this;
    }

    public KeelMySQLSelectSQLBuilder whereForRaw(Function<RawCondition, RawCondition> f) {
        RawCondition condition = new RawCondition();
        whereConditions.add(f.apply(condition));
        return this;
    }

    public KeelMySQLSelectSQLBuilder whereForCompare(Function<CompareCondition, CompareCondition> f) {
        CompareCondition condition = new CompareCondition();
        whereConditions.add(f.apply(condition));
        return this;
    }

    public KeelMySQLSelectSQLBuilder whereForAmongst(Function<AmongstCondition, AmongstCondition> f) {
        whereConditions.add(f.apply(new AmongstCondition()));
        return this;
    }

    public KeelMySQLSelectSQLBuilder whereForAndGroup(Function<GroupCondition, GroupCondition> f) {
        GroupCondition condition = new GroupCondition(GroupCondition.JUNCTION_FOR_AND);
        whereConditions.add(f.apply(condition));
        return this;
    }

    public KeelMySQLSelectSQLBuilder whereForOrGroup(Function<GroupCondition, GroupCondition> f) {
        GroupCondition condition = new GroupCondition(GroupCondition.JUNCTION_FOR_OR);
        whereConditions.add(f.apply(condition));
        return this;
    }

    public KeelMySQLSelectSQLBuilder groupBy(String x) {
        categories.add(x);
        return this;
    }

    public KeelMySQLSelectSQLBuilder groupBy(List<String> x) {
        categories.addAll(x);
        return this;
    }

    public KeelMySQLSelectSQLBuilder having(KeelMySQLCondition condition) {
        havingConditions.add(condition);
        return this;
    }

    public KeelMySQLSelectSQLBuilder orderByAsc(String x) {
        sortRules.add(x);
        return this;
    }

    public KeelMySQLSelectSQLBuilder orderByDesc(String x) {
        sortRules.add(x + " DESC");
        return this;
    }

    public KeelMySQLSelectSQLBuilder limit(long limit) {
        this.offset = 0;
        this.limit = limit;
        return this;
    }

    public KeelMySQLSelectSQLBuilder limit(long limit, long offset) {
        this.offset = offset;
        this.limit = limit;
        return this;
    }

    public KeelMySQLSelectSQLBuilder setLockMode(String lockMode) {
        this.lockMode = lockMode;
        return this;
    }

    public String toString() {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT ").append(KeelHelper.joinStringArray(columns, ","));
        if (!tables.isEmpty()) {
            sql.append("\n").append("FROM ").append(KeelHelper.joinStringArray(tables, "\n"));
        }
        if (!whereConditions.isEmpty()) {
            sql.append("\n").append("WHERE ").append(KeelHelper.joinStringArray(whereConditions, "\nAND "));
        }
        if (!categories.isEmpty()) {
            sql.append("\n").append("GROUP BY ").append(KeelHelper.joinStringArray(categories, ","));
        }
        if (!havingConditions.isEmpty()) {
            sql.append("\n").append("HAVING ").append(KeelHelper.joinStringArray(havingConditions, " AND "));
        }
        if (!sortRules.isEmpty()) {
            sql.append("\n").append("ORDER BY ").append(KeelHelper.joinStringArray(sortRules, ","));
        }
        if (limit > 0) {
            sql.append("\n").append("LIMIT ").append(limit).append(" OFFSET ").append(offset);
        }
        if (!"".equals(lockMode)) {
            sql.append("\n").append(lockMode);
        }

        return String.valueOf(sql);
    }

    public static class JoinComponent {
        String joinType;
        String tableExpression;
        String alias;
        List<KeelMySQLCondition> onConditions = new ArrayList<>();

        public JoinComponent(String joinType) {
            this.joinType = joinType;
        }

        public JoinComponent table(String tableExpression) {
            this.tableExpression = tableExpression;
            return this;
        }

        public JoinComponent alias(String alias) {
            this.alias = alias;
            return this;
        }

        public JoinComponent onForRaw(Function<RawCondition, RawCondition> func) {
            this.onConditions.add(func.apply(new RawCondition()));
            return this;
        }

        public JoinComponent onForAndGroup(Function<GroupCondition, GroupCondition> func) {
            this.onConditions.add(func.apply(new GroupCondition(GroupCondition.JUNCTION_FOR_AND)));
            return this;
        }

        public JoinComponent onForOrGroup(Function<GroupCondition, GroupCondition> func) {
            this.onConditions.add(func.apply(new GroupCondition(GroupCondition.JUNCTION_FOR_OR)));
            return this;
        }

        public JoinComponent onForCompare(Function<CompareCondition, CompareCondition> func) {
            this.onConditions.add(func.apply(new CompareCondition()));
            return this;
        }

        public String toString() {
            String s = joinType + " " + tableExpression;
            if (alias != null) {
                s += " AS " + alias;
            }
            if (onConditions != null && !onConditions.isEmpty()) {
                s += " ON ";
                s += KeelHelper.joinStringArray(onConditions, " AND ");
            }
            return s;
        }
    }

    public static class ColumnComponent {
        String schema;
        String field;
        String expression;
        String alias;

        public ColumnComponent field(String field) {
            this.field = field;
            return this;
        }

        public ColumnComponent field(String schema, String field) {
            this.schema = schema;
            this.field = field;
            return this;
        }

        public ColumnComponent expression(String expression) {
            this.expression = expression;
            return this;
        }

        public ColumnComponent alias(String alias) {
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
