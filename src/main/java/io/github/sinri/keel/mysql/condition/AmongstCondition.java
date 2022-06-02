package io.github.sinri.keel.mysql.condition;

import io.github.sinri.keel.core.KeelHelper;
import io.github.sinri.keel.mysql.KeelMySQLQuoter;
import io.github.sinri.keel.mysql.exception.KeelSQLGenerateError;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class AmongstCondition extends KeelMySQLCondition {
    public static final String OP_IN = "IN";

    protected String element;
    protected final List<String> targetSet;
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
     */
    public AmongstCondition element(Object element, Boolean needQuoting) {
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
     */
    public AmongstCondition element(Object element) {
        return element(element, false);
    }

    public AmongstCondition elementAsExpression(String element) {
        this.element = element;
        return this;
    }

    public AmongstCondition elementAsValue(String element) {
        this.element = new KeelMySQLQuoter(element).toString();
        return this;
    }

    public AmongstCondition elementAsValue(Number element) {
        this.element = new KeelMySQLQuoter(element).toString();
        return this;
    }

    /**
     * @since 1.4
     */
    public AmongstCondition amongst(Collection<?> targetSet, boolean needQuoting) {
        if (needQuoting) {
            return amongstValueList(targetSet);
        } else {
            List<String> x = new ArrayList<>();
            for (var y : targetSet) {
                x.add(y.toString());
            }
            return amongstExpression(x);
        }
    }

    /**
     * @since 1.4
     */
    public AmongstCondition amongst(Collection<?> targetSet) {
        return amongst(targetSet, true);
    }

    public AmongstCondition amongstValueList(Collection<?> targetSet) {
        for (Object next : targetSet) {
            this.targetSet.add(new KeelMySQLQuoter(String.valueOf(next)).toString());
        }
        return this;
    }

    public AmongstCondition amongstValueArray(Object[] targetSet) {
        for (Object next : targetSet) {
            this.targetSet.add(new KeelMySQLQuoter(String.valueOf(next)).toString());
        }
        return this;
    }

    public AmongstCondition amongstValue(String value) {
        this.targetSet.add(new KeelMySQLQuoter(value).toString());
        return this;
    }

    public AmongstCondition amongstValue(Number value) {
        this.targetSet.add(new KeelMySQLQuoter(value).toString());
        return this;
    }

    public AmongstCondition amongstExpression(String value) {
        this.targetSet.add(value);
        return this;
    }

    public AmongstCondition amongstExpression(List<String> value) {
        this.targetSet.addAll(value);
        return this;
    }

    @Override
    public String toString() {
        if (targetSet.isEmpty()) {
            throw new KeelSQLGenerateError("AmongstCondition Target Set Empty");
        }

        String s = element;
        if (inverseOperator) {
            s += " NOT";
        }
        s += " " + OP_IN + " (" + KeelHelper.joinStringArray(targetSet, ",") + ")";
        return s;
    }
}
