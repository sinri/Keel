package io.github.sinri.keel.mysql.statement;

import io.github.sinri.keel.mysql.condition.CompareCondition;
import io.vertx.core.json.JsonArray;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * @since 1.13
 */
public class CatholicQueryCriteria {
    protected final Map<String, JsonArray> inclusiveCriteria = new HashMap<>();
    protected final Map<String, JsonArray> exclusiveCriteria = new HashMap<>();

    public <T> CatholicQueryCriteria include(String name, T value) {
        if (value == null) return this;
        inclusiveCriteria.computeIfAbsent(name, x -> new JsonArray());
        inclusiveCriteria.get(name).add(value);
        return this;
    }

    public <T> CatholicQueryCriteria include(String name, Collection<T> values) {
        if (values == null) return this;
        inclusiveCriteria.computeIfAbsent(name, x -> new JsonArray());
        JsonArray jsonArray = new JsonArray();
        values.forEach(jsonArray::add);
        inclusiveCriteria.get(name).addAll(jsonArray);
        return this;
    }

    public CatholicQueryCriteria include(String name, JsonArray values) {
        if (values == null) return this;
        inclusiveCriteria.computeIfAbsent(name, x -> new JsonArray());
        inclusiveCriteria.get(name).addAll(values);
        return this;
    }

    public <T> CatholicQueryCriteria exclude(String name, T value) {
        if (value == null) return this;
        exclusiveCriteria.computeIfAbsent(name, x -> new JsonArray());
        exclusiveCriteria.get(name).add(value);
        return this;
    }

    public <T> CatholicQueryCriteria exclude(String name, Collection<T> values) {
        if (values == null) return this;
        exclusiveCriteria.computeIfAbsent(name, x -> new JsonArray());
        JsonArray jsonArray = new JsonArray();
        values.forEach(jsonArray::add);
        exclusiveCriteria.get(name).addAll(jsonArray);
        return this;
    }

    public CatholicQueryCriteria exclude(String name, JsonArray values) {
        if (values == null) return this;
        exclusiveCriteria.computeIfAbsent(name, x -> new JsonArray());
        exclusiveCriteria.get(name).addAll(values);
        return this;
    }

    public final ConditionsComponent mergeIntoConditionsComponent(ConditionsComponent conditionsComponent) {
        inclusiveCriteria.forEach((name, objects) -> {
            if (objects.isEmpty()) return;
            if (objects.size() == 1) {
                conditionsComponent.comparison(compareCondition -> compareCondition
                        .filedEqualsValue(name, objects.getValue(0))
                );
            } else {
                conditionsComponent.among(amongstCondition -> amongstCondition
                        .elementAsExpression(name)
                        .amongstValueList(objects.getList())
                );
            }
        });
        exclusiveCriteria.forEach((name, objects) -> {
            if (objects.isEmpty()) return;
            if (objects.size() == 1) {
                conditionsComponent.comparison(compareCondition -> compareCondition
                        .compare(name)
                        .operator(CompareCondition.OP_NEQ)
                        .againstValue(objects.getValue(0))
                );
            } else {
                conditionsComponent.among(amongstCondition -> amongstCondition
                        .elementAsExpression(name)
                        .not()
                        .amongstValueList(objects.getList())
                );
            }
        });
        return conditionsComponent;
    }
}
