package io.github.sinri.keel.mysql.statement.component;

import io.github.sinri.keel.core.TechnicalPreview;
import io.github.sinri.keel.mysql.Quoter;
import io.github.sinri.keel.mysql.exception.KeelMySQLGenerateError;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


/**
 * @since 3.0.19
 */
@TechnicalPreview(since = "3.0.19")
public class UpdateSetAssignmentComponent {
    private final @NotNull String fieldName;
    private String expression;

    public UpdateSetAssignmentComponent(@NotNull String fieldName) {
        this.fieldName = fieldName;
    }

    public UpdateSetAssignmentComponent assignmentToExpression(@NotNull String expression) {
        this.expression = expression;
        return this;
    }

    public UpdateSetAssignmentComponent assignmentToValue(@Nullable Object expression) {
        if (expression == null) {
            this.expression = "NULL";
        } else if (expression instanceof Number) {
            this.expression = expression.toString();
        } else {
            this.expression = new Quoter(expression.toString()).toString();
        }
        return this;
    }

    public UpdateSetAssignmentComponent assignmentToNull() {
        this.expression = "NULL";
        return this;
    }

    public UpdateSetAssignmentComponent assignmentToCaseOperator(@NotNull CaseOperator caseOperator) {
        this.expression = caseOperator.toString();
        return this;
    }

    @Override
    public String toString() {
        if (expression == null) {
            throw new KeelMySQLGenerateError("The expression in UPDATE SET STATEMENT is not set.");
        }
        return fieldName + "=" + expression;
    }

}
