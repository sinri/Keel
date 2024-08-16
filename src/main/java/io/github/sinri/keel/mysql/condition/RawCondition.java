package io.github.sinri.keel.mysql.condition;


import org.jetbrains.annotations.NotNull;

public class RawCondition implements MySQLCondition {
    @NotNull
    private String rawConditionExpression;

    public RawCondition() {
        this.rawConditionExpression = "";
    }

    public RawCondition(@NotNull String rawConditionExpression) {
        this.rawConditionExpression = rawConditionExpression;
    }

    public void setRawConditionExpression(@NotNull String rawConditionExpression) {
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
