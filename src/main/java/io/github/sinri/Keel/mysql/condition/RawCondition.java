package io.github.sinri.Keel.mysql.condition;

public class RawCondition extends KeelMySQLCondition {
    private String rawConditionExpression;

    public RawCondition() {
        this.rawConditionExpression = "";
    }

    public RawCondition(String rawConditionExpression) {
        this.rawConditionExpression = rawConditionExpression;
    }

    public void setRawConditionExpression(String rawConditionExpression) {
        this.rawConditionExpression = rawConditionExpression;
    }

    @Override
    public String toString() {
        return rawConditionExpression;
    }
}
