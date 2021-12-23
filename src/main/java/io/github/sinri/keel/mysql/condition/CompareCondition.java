package io.github.sinri.keel.mysql.condition;

import io.github.sinri.keel.mysql.KeelMySQLQuoter;

public class CompareCondition extends KeelMySQLCondition {
    public static final String OP_EQ = "=";
    public static final String OP_NEQ = "<>";
    public static final String OP_NULL_SAFE_EQ = "<=>";
    public static final String OP_GT = ">";
    public static final String OP_EGT = ">=";
    public static final String OP_LT = "<";
    public static final String OP_ELT = "<=";
    public static final String OP_IS = "IS";
    //    public static final String OP_IS_NOT="IS NOT";
    public static final String OP_LIKE = "LIKE";
//    public static final String OP_NOT_LIKE="NOT LIKE";

    protected String leftSide;
    protected String operator;
    protected String rightSide;
    protected boolean inverseOperator;

    public CompareCondition() {
        this.leftSide = null;
        this.operator = null;
        this.rightSide = null;
        this.inverseOperator = false;
    }

    public CompareCondition(String operator) {
        this.leftSide = null;
        this.operator = operator;
        this.rightSide = null;
        this.inverseOperator = false;
    }

    public CompareCondition not() {
        this.inverseOperator = true;
        return this;
    }

    public CompareCondition compareExpression(Object leftSide) {
        this.leftSide = leftSide.toString();
        return this;
    }

    public CompareCondition compareValue(Object leftSide) {
        this.leftSide = String.valueOf(new KeelMySQLQuoter(leftSide));
        return this;
    }

    public CompareCondition operator(String operator) {
        this.operator = operator;
        return this;
    }

    public CompareCondition againstExpression(Object rightSide) {
        this.rightSide = rightSide.toString();
        return this;
    }

    public CompareCondition againstValue(Object rightSide) {
        this.rightSide = String.valueOf(new KeelMySQLQuoter(rightSide));
        return this;
    }

    public CompareCondition isNull() {
        this.operator = OP_IS;
        this.rightSide = "NULL";
        return this;
    }

    public CompareCondition isTrue() {
        this.operator = OP_IS;
        this.rightSide = "TRUE";
        return this;
    }

    public CompareCondition isFalse() {
        this.operator = OP_IS;
        this.rightSide = "FALSE";
        return this;
    }

    public CompareCondition isUnknown() {
        this.operator = OP_IS;
        this.rightSide = "UNKNOWN";
        return this;
    }

    public CompareCondition contains(String rightSide) {
        this.operator = OP_LIKE;
        String x = KeelMySQLQuoter.escapeStringWithWildcards(rightSide);
        this.rightSide = "'%" + x + "%'";
        return this;
    }

    public CompareCondition hasPrefix(String rightSide) {
        this.operator = "like";
        String x = KeelMySQLQuoter.escapeStringWithWildcards(rightSide);
        this.rightSide = "'" + x + "%'";
        return this;
    }

    public CompareCondition hasSuffix(String rightSide) {
        this.operator = "like";
        String x = KeelMySQLQuoter.escapeStringWithWildcards(rightSide);
        this.rightSide = "'%" + x + "'";
        return this;
    }

    @Override
    public String toString() {
        String x = leftSide + " " + operator + " " + rightSide;
        if (inverseOperator) {
            x = "NOT (" + x + ")";
        }
        return x;
    }
}
