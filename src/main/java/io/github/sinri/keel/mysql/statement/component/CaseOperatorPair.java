package io.github.sinri.keel.mysql.statement.component;

import io.github.sinri.keel.core.TechnicalPreview;
import io.github.sinri.keel.mysql.Quoter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


/**
 * @since 3.0.19
 */
@TechnicalPreview(since = "3.0.19")
public class CaseOperatorPair {
    private String whenExpression;
    private String thenExpression;

    public CaseOperatorPair() {

    }

    public CaseOperatorPair setThenAsNumber(@NotNull Number thenAsNumber) {
        this.thenExpression = String.valueOf(thenAsNumber);
        return this;
    }

    public CaseOperatorPair setThenAsString(@Nullable String thenAsString) {
        this.thenExpression = new Quoter(thenAsString).toString();
        return this;
    }

    public CaseOperatorPair setWhenAsNumber(@NotNull Number whenAsNumber) {
        this.whenExpression = String.valueOf(whenAsNumber);
        return this;
    }

    public CaseOperatorPair setWhenAsString(@Nullable String whenAsString) {
        this.whenExpression = new Quoter(whenAsString).toString();
        return this;
    }

    @NotNull
    public String getWhenExpression() {
        return whenExpression;
    }

    public CaseOperatorPair setWhenExpression(@NotNull String whenExpression) {
        this.whenExpression = whenExpression;
        return this;
    }

    @NotNull
    public String getThenExpression() {
        return thenExpression;
    }

    public CaseOperatorPair setThenExpression(@NotNull String thenExpression) {
        this.thenExpression = thenExpression;
        return this;
    }
}
