package io.github.sinri.keel.mysql.condition;

import io.github.sinri.keel.mysql.exception.KeelSQLGenerateError;

import java.util.ArrayList;
import java.util.List;

public class GroupCondition implements MySQLCondition {
    public static final String JUNCTION_FOR_AND = "AND";
    public static final String JUNCTION_FOR_OR = "OR";

    protected final List<MySQLCondition> conditions = new ArrayList<>();
    protected final String junction;

    public GroupCondition(String junction) {
        this.junction = junction;
    }

    public GroupCondition(String junction, List<MySQLCondition> conditions) {
        this.junction = junction;
        this.conditions.addAll(conditions);
    }

    public GroupCondition add(MySQLCondition condition) {
        this.conditions.add(condition);
        return this;
    }

    public GroupCondition add(List<MySQLCondition> conditions) {
        this.conditions.addAll(conditions);
        return this;
    }

    /**
     * 生成SQL的组合逻辑条件表达式文本。如果出错，则抛出 KeelSQLGenerateError 异常。
     *
     * @throws KeelSQLGenerateError sql generate error
     */
    @Override
    public String toString() {
        if (conditions.isEmpty()) {
            return "";
        }
        StringBuilder x = new StringBuilder();
        for (MySQLCondition condition : conditions) {
            if (x.length() > 0) {
                x.append(" ").append(junction).append(" ");
            }
            x.append(condition);
        }
        return "(" + x + ")";
    }
}
