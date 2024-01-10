package io.github.sinri.keel.mysql.statement.component;

import io.github.sinri.keel.core.TechnicalPreview;
import io.github.sinri.keel.mysql.Quoter;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * {@code NULLIF(expr1,expr2) }
 * <p>
 * Returns NULL if expr1 = expr2 is true, otherwise returns expr1. This is the same as CASE WHEN expr1 = expr2 THEN NULL ELSE expr1 END.
 *
 * @since 3.0.19
 */
@TechnicalPreview(since = "3.0.19")
public class NullIfOperator {
    private String baseExpression;
    private String nullRefExpression;


    public NullIfOperator setBaseAsNumber(@Nullable Number number) {
        this.baseExpression = String.valueOf(number);
        return this;
    }

    public NullIfOperator setBaseAsString(@Nullable String string) {
        this.baseExpression = new Quoter(string).toString();
        return this;
    }

    public NullIfOperator setBaseExpression(@Nonnull String baseExpression) {
        this.baseExpression = baseExpression;
        return this;
    }

    public NullIfOperator setNullRefNumber(@Nullable Number number) {
        this.nullRefExpression = String.valueOf(number);
        return this;
    }

    public NullIfOperator setNullRefString(@Nullable String string) {
        this.nullRefExpression = new Quoter(string).toString();
        return this;
    }

    public NullIfOperator setNullRefExpression(@Nonnull String nullRefExpression) {
        this.nullRefExpression = nullRefExpression;
        return this;
    }

    @Override
    public String toString() {
        return "NULLIF(" + baseExpression + "," + nullRefExpression + ")";
    }
}
