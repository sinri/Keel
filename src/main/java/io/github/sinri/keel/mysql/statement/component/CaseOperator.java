package io.github.sinri.keel.mysql.statement.component;

import io.github.sinri.keel.core.TechnicalPreview;
import io.github.sinri.keel.mysql.Quoter;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

/**
 * <p>
 * {@code CASE value WHEN compare_value THEN result [WHEN compare_value THEN result ...] [ELSE result] END }
 * </p>
 * <p>
 * {@code CASE WHEN condition THEN result [WHEN condition THEN result ...] [ELSE result] END }
 * </p>
 *
 * @see <a href="https://dev.mysql.com/doc/refman/8.0/en/flow-control-functions.html#operator_case">
 * Case operator</a>
 * @since 3.0.19
 */
@TechnicalPreview(since = "3.0.19")
public class CaseOperator {
    private final Collection<CaseOperatorPair> whenThenPairs;
    private String caseValueExpression = null;
    private String elseResultExpression = null;

    public CaseOperator() {
        this.whenThenPairs = new ArrayList<>();
    }

    public CaseOperator setCaseValueAsNumber(@Nonnull Number caseValueAsNumber) {
        this.caseValueExpression = String.valueOf(caseValueAsNumber);
        return this;
    }

    public CaseOperator setCaseValueAsString(@Nonnull String caseValueAsString) {
        this.caseValueExpression = new Quoter(caseValueAsString).toString();
        return this;
    }

    public CaseOperator setElseResultAsNumber(@Nonnull String elseResultAsNumber) {
        this.elseResultExpression = elseResultAsNumber;
        return this;
    }

    public CaseOperator setElseResultAsString(@Nonnull String elseResultAsString) {
        this.elseResultExpression = new Quoter(elseResultAsString).toString();
        return this;
    }

    public CaseOperator addWhenThenPair(@Nonnull CaseOperatorPair caseOperatorPair) {
        this.whenThenPairs.add(caseOperatorPair);
        return this;
    }

    @Nullable
    public String getCaseValueExpression() {
        return caseValueExpression;
    }

    public CaseOperator setCaseValueExpression(@Nonnull String caseValueExpression) {
        this.caseValueExpression = caseValueExpression;
        return this;
    }

    @Nonnull
    public Collection<CaseOperatorPair> getWhenThenPairs() {
        return whenThenPairs;
    }

    @Nullable
    public String getElseResultExpression() {
        return elseResultExpression;
    }

    public CaseOperator setElseResultExpression(@Nonnull String elseResultExpression) {
        this.elseResultExpression = elseResultExpression;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("CASE");
        if (!(Objects.requireNonNullElse(getCaseValueExpression(), "")).isBlank()) {
            sb.append(" ").append(getCaseValueExpression());
        }
        getWhenThenPairs().forEach(pair -> {
            sb.append(" WHEN ").append(pair.getWhenExpression())
                    .append(" THEN ").append(pair.getThenExpression());
        });
        if (!(Objects.requireNonNullElse(getElseResultExpression(), "")).isBlank()) {
            sb.append(" ELSE ").append(getElseResultExpression());
        }
        sb.append(" END");
        return sb.toString();
    }
}
