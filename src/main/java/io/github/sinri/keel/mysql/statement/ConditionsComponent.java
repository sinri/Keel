package io.github.sinri.keel.mysql.statement;

import io.github.sinri.keel.core.KeelHelper;
import io.github.sinri.keel.mysql.condition.*;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * @since 1.9 all the callback function could return null safely. by Sinri 2020-02-07
 */
public class ConditionsComponent {
    protected final List<KeelMySQLCondition> conditions;

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
        return KeelHelper.joinStringArray(conditions, " and ");
    }
}
