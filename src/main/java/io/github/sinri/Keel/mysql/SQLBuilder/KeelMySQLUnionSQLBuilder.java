package io.github.sinri.Keel.mysql.SQLBuilder;

import io.github.sinri.Keel.core.KeelHelper;

import java.util.ArrayList;
import java.util.List;

public class KeelMySQLUnionSQLBuilder {
    List<String> selections = new ArrayList<>();

    public KeelMySQLUnionSQLBuilder() {

    }

    public KeelMySQLUnionSQLBuilder(String firstSelection) {
        selections.add("(\n" + firstSelection + "\n)");
    }

    public KeelMySQLUnionSQLBuilder union(String selection) {
        if (this.selections.isEmpty()) {
            selections.add("(\n" + selection + "\n)");
        } else {
            selections.add(" UNION (\n" + selection + "\n)");
        }
        return this;
    }

    public KeelMySQLUnionSQLBuilder unionAll(String selection) {
        if (this.selections.isEmpty()) {
            selections.add("(\n" + selection + "\n)");
        } else {
            selections.add(" UNION ALL (\n" + selection + "\n)");
        }
        return this;
    }

    public KeelMySQLUnionSQLBuilder union(List<String> list) {
        for (String selection : list) {
            union(selection);
        }
        return this;
    }

    public KeelMySQLUnionSQLBuilder unionAll(List<String> list) {
        for (String selection : list) {
            unionAll(selection);
        }
        return this;
    }

    public String toString() {
        return KeelHelper.joinStringArray(selections, " ");
    }
}
