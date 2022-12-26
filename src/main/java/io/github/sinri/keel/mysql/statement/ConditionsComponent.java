package io.github.sinri.keel.mysql.statement;

import io.github.sinri.keel.helper.KeelHelpers;
import io.github.sinri.keel.mysql.condition.*;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * @since 1.9 all the callback function could return null safely. by Sinri 2020-02-07
 */
public class ConditionsComponent {
    protected final List<MySQLCondition> conditions;

    public ConditionsComponent() {
        conditions = new ArrayList<>();
    }

    public boolean isEmpty() {
        return this.conditions.isEmpty();
    }

    public ConditionsComponent comparison(Function<CompareCondition, CompareCondition> function) {
        CompareCondition condition = function.apply(new CompareCondition());
        if (condition != null) {
            conditions.add(condition);
        }
        return this;
    }

    public ConditionsComponent comparison(String operator, Function<CompareCondition, CompareCondition> function) {
        CompareCondition condition = function.apply(new CompareCondition(operator));
        if (condition != null) {
            conditions.add(condition);
        }
        return this;
    }

    public ConditionsComponent among(Function<AmongstCondition, AmongstCondition> function) {
        AmongstCondition condition = function.apply(new AmongstCondition());
        if (condition != null) {
            conditions.add(condition);
        }
        return this;
    }

    public ConditionsComponent intersection(Function<GroupCondition, GroupCondition> function) {
        GroupCondition condition = function.apply(new GroupCondition(GroupCondition.JUNCTION_FOR_AND));
        if (condition != null) {
            conditions.add(condition);
        }
        return this;
    }

    public ConditionsComponent union(Function<GroupCondition, GroupCondition> function) {
        GroupCondition condition = function.apply(new GroupCondition(GroupCondition.JUNCTION_FOR_OR));
        if (condition != null) {
            conditions.add(condition);
        }
        return this;
    }

    public ConditionsComponent raw(String raw) {
        if (raw != null && !raw.isEmpty()) {
            conditions.add(new RawCondition(raw));
        }
        return this;
    }

    /**
     * @param catholicQueryCriteria CatholicQueryCriteria Instance
     * @return this
     * @since 1.13
     */
    public ConditionsComponent withCatholicQueryCriteria(CatholicQueryCriteria catholicQueryCriteria) {
        return catholicQueryCriteria.mergeIntoConditionsComponent(this);
    }

    @Override
    public String toString() {
        if (conditions.isEmpty()) return "";
        return KeelHelpers.stringHelper().joinStringArray(conditions, " and ");
    }

    /**
     * @since 2.0
     */
    public final ConditionsComponent quickMapping(JsonObject mapping) {
        mapping.forEach(entry -> quickMapping(entry.getKey(), entry.getValue()));
        return this;
    }

    /**
     * @since 2.0
     */
    public final ConditionsComponent quickMapping(String key, Object value) {
        if (key != null && !key.isEmpty()) {
            if (value == null) {
                this.comparison(compareCondition -> compareCondition
                        .compare(key)
                        .operator(CompareCondition.OP_IS)
                        .againstExpression("NULL")
                );
            } else if (value instanceof JsonArray) {
                if (((JsonArray) value).size() > 0) {
                    this.among(amongstCondition -> amongstCondition
                            .elementAsExpression(key)
                            .amongstValueList(((JsonArray) value).getList())
                    );
                }
            } else {
                this.comparison(compareCondition -> compareCondition
                        .filedEqualsValue(key, value.toString())
                );
            }
        }
        return this;
    }
}
