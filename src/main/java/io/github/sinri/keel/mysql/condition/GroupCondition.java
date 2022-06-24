package io.github.sinri.keel.mysql.condition;

import java.util.ArrayList;
import java.util.List;

public class GroupCondition implements KeelMySQLCondition {
    public static final String JUNCTION_FOR_AND = "AND";
    public static final String JUNCTION_FOR_OR = "OR";

    protected final List<KeelMySQLCondition> conditions = new ArrayList<>();
    protected final String junction;

    public GroupCondition(String junction) {
        this.junction = junction;
    }

    public GroupCondition(String junction, List<KeelMySQLCondition> conditions) {
        this.junction = junction;
        this.conditions.addAll(conditions);
    }

    public GroupCondition add(KeelMySQLCondition condition) {
        this.conditions.add(condition);
        return this;
    }

    public GroupCondition add(List<KeelMySQLCondition> conditions) {
        this.conditions.addAll(conditions);
        return this;
    }

    @Override
    public String toString() {
        if (conditions.isEmpty()) {
            return "";
        }
        StringBuilder x = new StringBuilder();
        for (KeelMySQLCondition condition : conditions) {
            if (x.length() > 0) {
                x.append(" ").append(junction).append(" ");
            }
            x.append(condition);
        }
        return "(" + x + ")";
    }
}
