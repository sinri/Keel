package io.github.sinri.keel.mysql.statement.component;

import io.github.sinri.keel.core.TechnicalPreview;
import io.github.sinri.keel.mysql.Quoter;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @since 3.0.19
 */
@TechnicalPreview(since = "3.0.19")
public class UpdateSetAssignmentComponent {
    private final @Nonnull String fieldName;
    private @Nonnull String expression;

    public UpdateSetAssignmentComponent(@Nonnull String fieldName) {
        this.fieldName = fieldName;
    }

    public UpdateSetAssignmentComponent assignmentToExpression(@Nonnull String expression) {
        this.expression = expression;
        return this;
    }

    public UpdateSetAssignmentComponent assignmentToValue(@Nullable Object expression) {
        if(expression==null){
            this.expression = "NULL";
        }else if (expression instanceof Number){
            this.expression = expression.toString();
        }else{
            this.expression = new Quoter(expression.toString()).toString();
        }
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
