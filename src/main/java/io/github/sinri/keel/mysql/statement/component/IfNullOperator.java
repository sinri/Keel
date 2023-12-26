package io.github.sinri.keel.mysql.statement.component;

import io.github.sinri.keel.core.TechnicalPreview;
import io.github.sinri.keel.mysql.Quoter;

import javax.annotation.Nullable;

/**
 * {@code IFNULL(expr1,expr2)}
 * <p>
 * If expr1 is not NULL, IFNULL() returns expr1; otherwise it returns expr2.
 * </p>
 *
 * @since 3.0.19
 */
@TechnicalPreview(since = "3.0.19")
public class IfNullOperator {
    private String conditionExpression;
    private String expressionForNull;


    public IfNullOperator setConditionAsNumber(@Nullable Number number) {
        this.conditionExpression = String.valueOf(number);
        return this;
    }

    public IfNullOperator setConditionAsString(@Nullable String string) {
        this.conditionExpression = new Quoter(string).toString();
        return this;
    }

    public IfNullOperator setConditionExpression(String conditionExpression) {
        this.conditionExpression = conditionExpression;
        return this;
    }

    public IfNullOperator setNumberForNull(@Nullable Number numberForNull) {
        this.expressionForNull = String.valueOf(numberForNull);
        return this;
    }

    public IfNullOperator setStringForNull(@Nullable String stringForNull) {
        this.expressionForNull = new Quoter(stringForNull).toString();
        return this;
    }

    public IfNullOperator setExpressionForNull(String expressionForNull) {
        this.expressionForNull = expressionForNull;
        return this;
    }

    @Override
    public String toString() {
        return "IFNULL(" + conditionExpression + "," + expressionForNull + ")";
    }
}
