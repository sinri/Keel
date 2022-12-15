package io.github.sinri.keel.mysql.statement;


import io.github.sinri.keel.facade.Keel;

import java.util.ArrayList;
import java.util.List;

public class UnionStatement extends AbstractReadStatement {
    final List<String> selections = new ArrayList<>();

    public UnionStatement() {

    }

    public UnionStatement(String firstSelection) {
        selections.add("(" + AbstractStatement.SQL_COMPONENT_SEPARATOR + firstSelection + AbstractStatement.SQL_COMPONENT_SEPARATOR + ")");
    }

    public UnionStatement union(String selection) {
        if (this.selections.isEmpty()) {
            selections.add("(" + AbstractStatement.SQL_COMPONENT_SEPARATOR + selection + AbstractStatement.SQL_COMPONENT_SEPARATOR + ")");
        } else {
            selections.add(" UNION (" + AbstractStatement.SQL_COMPONENT_SEPARATOR + selection + AbstractStatement.SQL_COMPONENT_SEPARATOR + ")");
        }
        return this;
    }

    public UnionStatement unionAll(String selection) {
        if (this.selections.isEmpty()) {
            selections.add("(" + AbstractStatement.SQL_COMPONENT_SEPARATOR + selection + AbstractStatement.SQL_COMPONENT_SEPARATOR + ")");
        } else {
            selections.add(" UNION ALL (" + AbstractStatement.SQL_COMPONENT_SEPARATOR + selection + AbstractStatement.SQL_COMPONENT_SEPARATOR + ")");
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
        return Keel.getInstance().stringHelper().joinStringArray(selections, " ") + (
                getRemarkAsComment().isEmpty() ? "" : ("\n-- " + getRemarkAsComment())
        );
    }

}
