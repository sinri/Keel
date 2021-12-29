package io.github.sinri.keel.mysql.statement;

import io.github.sinri.keel.core.KeelHelper;
import io.github.sinri.keel.mysql.condition.*;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class ConditionsComponent {
    protected final List<KeelMySQLCondition> conditions;

    public ConditionsComponent() {
        conditions = new ArrayList<>();
    }

    public boolean isEmpty() {
        return this.conditions.isEmpty();
    }

    public ConditionsComponent comparison(Function<CompareCondition, CompareCondition> function) {
        conditions.add(function.apply(new CompareCondition()));
        return this;
    }

    public ConditionsComponent comparison(String operator, Function<CompareCondition, CompareCondition> function) {
        conditions.add(function.apply(new CompareCondition(operator)));
        return this;
    }

    public ConditionsComponent among(Function<AmongstCondition, AmongstCondition> function) {
        conditions.add(function.apply(new AmongstCondition()));
        return this;
    }

    public ConditionsComponent intersection(Function<GroupCondition, GroupCondition> function) {
        conditions.add(function.apply(new GroupCondition(GroupCondition.JUNCTION_FOR_AND)));
        return this;
    }

    public ConditionsComponent union(Function<GroupCondition, GroupCondition> function) {
        conditions.add(function.apply(new GroupCondition(GroupCondition.JUNCTION_FOR_OR)));
        return this;
    }

    public ConditionsComponent raw(String raw) {
        conditions.add(new RawCondition(raw));
        return this;
    }

    @Override
    public String toString() {
        if (conditions.isEmpty()) return "";
        return KeelHelper.joinStringArray(conditions, " and ");
    }
}
