package io.github.sinri.keel.mysql.statement;

import io.github.sinri.keel.core.KeelHelper;

import java.util.ArrayList;
import java.util.List;

public class UnionStatement {
    final List<String> selections = new ArrayList<>();

    public UnionStatement() {

    }

    public UnionStatement(String firstSelection) {
        selections.add("(\n" + firstSelection + "\n)");
    }

    public UnionStatement union(String selection) {
        if (this.selections.isEmpty()) {
            selections.add("(\n" + selection + "\n)");
        } else {
            selections.add(" UNION (\n" + selection + "\n)");
        }
        return this;
    }

    public UnionStatement unionAll(String selection) {
        if (this.selections.isEmpty()) {
            selections.add("(\n" + selection + "\n)");
        } else {
            selections.add(" UNION ALL (\n" + selection + "\n)");
        }
        return this;
    }

    public UnionStatement union(List<String> list) {
        for (String selection : list) {
            union(selection);
        }
        return this;
    }

    public UnionStatement unionAll(List<String> list) {
        for (String selection : list) {
            unionAll(selection);
        }
        return this;
    }

    public String toString() {
        return KeelHelper.joinStringArray(selections, " ");
    }
}
