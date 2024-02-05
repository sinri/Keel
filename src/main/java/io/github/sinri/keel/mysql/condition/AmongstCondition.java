package io.github.sinri.keel.mysql.condition;

import io.github.sinri.keel.mysql.Quoter;
import io.github.sinri.keel.mysql.exception.KeelSQLGenerateError;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import static io.github.sinri.keel.helper.KeelHelpersInterface.KeelHelpers;

public class AmongstCondition implements MySQLCondition {
    public static final String OP_IN = "IN";
    protected final List<String> targetSet;
    protected String element;
    protected boolean inverseOperator;

    public AmongstCondition() {
        this.inverseOperator = false;
        this.targetSet = new ArrayList<>();
    }

    public AmongstCondition not() {
        this.inverseOperator = true;
        return this;
    }

    /**
     * @param element     expression or value
     * @param needQuoting TRUE for VALUE, FALSE for EXPRESSION
     * @return AmongstCondition
     * @since 1.4
     * @deprecated use any method named `elementAs[TYPE]` instead.
     */
    @Deprecated(since = "3.1.8", forRemoval = true)
    public AmongstCondition element(@Nonnull Object element, boolean needQuoting) {
        if (needQuoting) {
            if (element instanceof Number) {
                return elementAsValue((Number) element);
            } else {
                return elementAsValue(String.valueOf(element));
            }
        } else {
            return elementAsExpression(String.valueOf(element));
        }
    }

    /**
     * @param element expression (would not be quoted)
     * @return AmongstCondition
     * @since 1.4
     * @deprecated use any method named `elementAsExpression` instead.
     */
    @Deprecated(since = "3.1.8", forRemoval = true)
    public AmongstCondition element(@Nonnull Object element) {
        return element(element, false);
    }

    public AmongstCondition elementAsExpression(@Nonnull String element) {
        this.element = element;
        return this;
    }

    public AmongstCondition elementAsValue(@Nullable String element) {
        this.element = new Quoter(element).toString();
        return this;
    }

    public AmongstCondition elementAsValue(@Nullable Number element) {
        this.element = new Quoter(element).toString();
        return this;
    }

    /**
     * @since 1.4
     * @deprecated use method `amongst[Type]List` instead.
     */
    @Deprecated(since = "3.1.8", forRemoval = true)
    public AmongstCondition amongst(@Nonnull Collection<?> targetSet, boolean needQuoting) {
        if (needQuoting) {
            return amongstValueList(targetSet);
        } else {
            List<String> x = new ArrayList<>();
            for (var y : targetSet) {
                x.add(y.toString());
            }
            return amongstExpressionList(x);
        }
    }

    /**
     * @since 1.4
     * @deprecated use method `amongstValueList` instead.
     */
    @Deprecated(since = "3.1.8", forRemoval = true)
    public AmongstCondition amongst(@Nonnull Collection<?> targetSet) {
        return amongst(targetSet, true);
    }

    /**
     * @deprecated @deprecated use method `amongstLiteralValueList` instead.
     */
    @Deprecated(since = "3.1.8", forRemoval = true)
    public AmongstCondition amongstValueList(@Nonnull Collection<?> targetSet) {
        for (Object next : targetSet) {
            this.targetSet.add(new Quoter(String.valueOf(next)).toString());
        }
        return this;
    }

    /**
     * @since 3.1.8
     */
    public AmongstCondition amongstLiteralValueList(@Nonnull Collection<?> targetSet) {
        for (Object next : targetSet) {
            //this.targetSet.add(new Quoter(String.valueOf(next)).toString());
            this.amongstLiteralValue(next);
        }
        return this;
    }

    /**
     * @since 3.1.8
     */
    public AmongstCondition amongstNumericValueList(@Nonnull Collection<? extends Number> targetSet) {
        for (Number next : targetSet) {
            //this.targetSet.add(new Quoter(String.valueOf(next)).toString());
            this.amongstNumericValue(next);
        }
        return this;
    }

    /**
     * @deprecated use method `amongstLiteralValueList` instead.
     */
    @Deprecated(since = "3.1.8", forRemoval = true)
    public AmongstCondition amongstValueArray(@Nonnull Object[] targetSet) {
        for (Object next : targetSet) {
            this.targetSet.add(new Quoter(String.valueOf(next)).toString());
        }
        return this;
    }

    /**
     * @deprecated use method `amongstLiteralValueList` instead.
     */
    @Deprecated(since = "3.1.8")
    public AmongstCondition amongstValue(@Nullable String value) {
        this.targetSet.add(new Quoter(value).toString());
        return this;
    }

    /**
     * @since 3.1.8
     */
    protected AmongstCondition amongstLiteralValue(@Nullable Object value) {
        if (value == null) {
            this.targetSet.add("NULL");
        } else {
            this.targetSet.add(new Quoter(String.valueOf(value)).toString());
        }
        return this;
    }

    /**
     * @deprecated use method `amongstNumericValueList` instead.
     */
    @Deprecated(since = "3.1.8", forRemoval = true)
    public AmongstCondition amongstValue(@Nullable Number value) {
        this.targetSet.add(new Quoter(value).toString());
        return this;
    }

    /**
     * @since 3.1.8
     */
    protected AmongstCondition amongstNumericValue(@Nullable Number value) {
        if (value == null) {
            this.targetSet.add("NULL");
        } else {
            if (value instanceof BigDecimal) {
                this.targetSet.add(((BigDecimal) value).toPlainString());
            } else {
                this.targetSet.add(value.toString());
            }
        }
        return this;
    }

    /**
     * @since 3.1.8 protected
     */
    protected AmongstCondition amongstExpression(@Nonnull String value) {
        this.targetSet.add(Objects.requireNonNull(value));
        return this;
    }

    /**
     * @since 3.1.8 renamed from `amongstExpression`
     */
    public AmongstCondition amongstExpressionList(@Nonnull List<String> values) {
        values.forEach(x -> this.amongstExpression(Objects.requireNonNull(x)));
        return this;
    }

    /**
     * 生成SQL的比较条件表达式文本。如果出错，则抛出 KeelSQLGenerateError 异常。
     *
     * @throws KeelSQLGenerateError sql generate error
     */
    @Override
    public String toString() {
        if (targetSet.isEmpty()) {
            throw new KeelSQLGenerateError("AmongstCondition Target Set Empty");
        }

        String s = element;
        if (inverseOperator) {
            s += " NOT";
        }
        s += " " + OP_IN + " (" + KeelHelpers.stringHelper().joinStringArray(targetSet, ",") + ")";
        return s;
    }
}
