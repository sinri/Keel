package io.github.sinri.keel.mysql.condition;

import javax.annotation.Nonnull;

public class RawCondition implements MySQLCondition {
    @Nonnull private String rawConditionExpression;

    public RawCondition() {
        this.rawConditionExpression = "";
    }

    public RawCondition(@Nonnull String rawConditionExpression) {
        this.rawConditionExpression = rawConditionExpression;
    }

    public void setRawConditionExpression(@Nonnull String rawConditionExpression) {
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
