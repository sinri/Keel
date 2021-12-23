package io.github.sinri.keel.mysql.condition;

import io.github.sinri.keel.core.KeelHelper;
import io.github.sinri.keel.mysql.KeelMySQLQuoter;
import io.github.sinri.keel.mysql.exception.KeelSQLGenerateError;

import java.util.ArrayList;
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

    public AmongstCondition elementAsExpression(String element) {
        this.element = element;
        return this;
    }

    public AmongstCondition elementAsValue(Object element) {
        this.element = new KeelMySQLQuoter(element).toString();
        return this;
    }

    public AmongstCondition amongstValue(List<Object> targetSet) {
        for (Object next : targetSet) {
            this.targetSet.add(new KeelMySQLQuoter(next).toString());
        }
        return this;
    }

    public AmongstCondition amongstValue(Object value) {
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
