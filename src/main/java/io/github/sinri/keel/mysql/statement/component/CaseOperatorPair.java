package io.github.sinri.keel.mysql.statement.component;

import io.github.sinri.keel.core.TechnicalPreview;
import io.github.sinri.keel.mysql.Quoter;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @since 3.0.19
 */
@TechnicalPreview(since = "3.0.19")
public class CaseOperatorPair {
    private String whenExpression;
    private String thenExpression;

    public CaseOperatorPair() {

    }

    public CaseOperatorPair setThenAsNumber(@Nonnull Number thenAsNumber) {
        this.thenExpression = String.valueOf(thenAsNumber);
        return this;
    }

    public CaseOperatorPair setThenAsString(@Nullable String thenAsString) {
        this.thenExpression = new Quoter(thenAsString).toString();
        return this;
    }

    public CaseOperatorPair setWhenAsNumber(@Nonnull Number whenAsNumber) {
        this.whenExpression = String.valueOf(whenAsNumber);
        return this;
    }

    public CaseOperatorPair setWhenAsString(@Nullable String whenAsString) {
        this.whenExpression = new Quoter(whenAsString).toString();
        return this;
    }

    @Nonnull
    public String getWhenExpression() {
        return whenExpression;
    }

    public CaseOperatorPair setWhenExpression(@Nonnull String whenExpression) {
        this.whenExpression = whenExpression;
        return this;
    }

    @Nonnull
    public String getThenExpression() {
        return thenExpression;
    }

    public CaseOperatorPair setThenExpression(@Nonnull String thenExpression) {
        this.thenExpression = thenExpression;
        return this;
    }
}
