package io.github.sinri.Keel.mysql.condition;

import io.github.sinri.Keel.core.KeelHelper;
import io.github.sinri.Keel.mysql.KeelMySQLQuoter;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class AmongstCondition extends KeelMySQLCondition {
    public static final String OP_IN = "IN";

    protected String element;
    protected List<String> targetSet = new ArrayList<>();
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
        Iterator<Object> iterator = targetSet.iterator();
        while (iterator.hasNext()) {
            Object next = iterator.next();
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
        String s = element;
        if (inverseOperator) {
            s += " not";
        }
        s += " in (" + KeelHelper.joinStringArray(targetSet, ",") + ")";
        return s;
    }
}
