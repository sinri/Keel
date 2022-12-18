package io.github.sinri.keel.mysql.condition;

public class RawCondition implements MySQLCondition {
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

    /**
     * 生成SQL的源格式表达式文本
     */
    @Override
    public String toString() {
        return rawConditionExpression;
    }
}
