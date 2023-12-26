package io.github.sinri.keel.mysql.statement;

import io.github.sinri.keel.core.TechnicalPreview;
import io.github.sinri.keel.helper.KeelHelpers;
import io.github.sinri.keel.mysql.statement.component.UpdateSetAssignmentComponent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class UpdateStatement extends AbstractModifyStatement {
    /**
     * @since 3.0.19 changed to use UpdateSetAssignmentComponent as item
     */
    final List<UpdateSetAssignmentComponent> assignments = new ArrayList<>();
    final ConditionsComponent whereConditionsComponent = new ConditionsComponent();
    final List<String> sortRules = new ArrayList<>();
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

    /**
     * @since 3.0.19
     */
    @TechnicalPreview(since = "3.0.19")
    public UpdateStatement setWithAssignment(UpdateSetAssignmentComponent updateSetAssignmentComponent) {
        this.assignments.add(updateSetAssignmentComponent);
        return this;
    }

    /**
     * @since 3.0.19
     */
    @TechnicalPreview(since = "3.0.19")
    public UpdateStatement setWithAssignments(Collection<UpdateSetAssignmentComponent> updateSetAssignmentComponents) {
        this.assignments.addAll(updateSetAssignmentComponents);
        return this;
    }

    public UpdateStatement setWithExpression(Map<String, String> columnExpressionMapping) {
        //columnExpressionMapping.forEach((k, v) -> assignments.add(k + "=" + v));
        columnExpressionMapping.forEach((k, v) -> assignments
                .add(new UpdateSetAssignmentComponent(k)
                        .assignmentToExpression(v)));
        return this;
    }

    public UpdateStatement setWithExpression(String column, String expression) {
        //assignments.add(column + "=" + expression);
        assignments.add(new UpdateSetAssignmentComponent(column).assignmentToExpression(expression));
        return this;
    }

    public UpdateStatement setWithValue(String column, Number value) {
        //assignments.add(column + "=" + (new Quoter(value)));
        assignments.add(new UpdateSetAssignmentComponent(column).assignmentToValue(value));
        return this;
    }

    public UpdateStatement setWithValue(String column, String value) {
        //assignments.add(column + "=" + (new Quoter(value)));
        assignments.add(new UpdateSetAssignmentComponent(column).assignmentToValue(value));
        return this;
    }

    public UpdateStatement where(Function<ConditionsComponent, ConditionsComponent> function) {
        function.apply(whereConditionsComponent);
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

        // since 3.0.19
        List<String> setPairs = new ArrayList<>();
        assignments.forEach(assignment -> {
            setPairs.add(assignment.toString());
        });
        sql += AbstractStatement.SQL_COMPONENT_SEPARATOR + "SET " + KeelHelpers.stringHelper().joinStringArray(setPairs, ", ");

        if (!whereConditionsComponent.isEmpty()) {
            sql += AbstractStatement.SQL_COMPONENT_SEPARATOR + "WHERE " + whereConditionsComponent;
        }
        if (!sortRules.isEmpty()) {
            sql += AbstractStatement.SQL_COMPONENT_SEPARATOR + "ORDER BY " + KeelHelpers.stringHelper().joinStringArray(sortRules, ",");
        }
        if (limit > 0) {
            sql += AbstractStatement.SQL_COMPONENT_SEPARATOR + "LIMIT " + limit;
        }
        if (!getRemarkAsComment().isEmpty()) {
            sql += "\n-- " + getRemarkAsComment() + "\n";
        }
        return sql;
    }
}
