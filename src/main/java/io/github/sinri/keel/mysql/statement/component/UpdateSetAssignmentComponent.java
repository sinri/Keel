package io.github.sinri.keel.mysql.statement.component;

import io.github.sinri.keel.core.TechnicalPreview;
import io.github.sinri.keel.mysql.Quoter;

import javax.annotation.Nonnull;

/**
 * @since 3.0.19
 */
@TechnicalPreview(since = "3.0.19")
public class UpdateSetAssignmentComponent {
    private final String fieldName;
    private String expression;

    public UpdateSetAssignmentComponent(@Nonnull String fieldName) {
        this.fieldName = fieldName;
    }

    public UpdateSetAssignmentComponent assignmentToExpression(@Nonnull String expression) {
        this.expression = expression;
        return this;
    }

    public UpdateSetAssignmentComponent assignmentToValue(@Nonnull String expression) {
        this.expression = new Quoter(expression).toString();
        return this;
    }

    public UpdateSetAssignmentComponent assignmentToValue(@Nonnull Number expression) {
        this.expression = expression.toString();
        return this;
    }

    public UpdateSetAssignmentComponent assignmentToNull() {
        this.expression = "NULL";
        return this;
    }

    public UpdateSetAssignmentComponent assignmentToCaseOperator(@Nonnull CaseOperator caseOperator) {
        this.expression = caseOperator.toString();
        return this;
    }

    @Override
    public String toString() {
        return fieldName + "=" + expression;
    }

}
