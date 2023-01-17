package io.github.sinri.keel.test.lab.mysql.table;

import java.util.Objects;

public class TableColumn {
    private final String column;
    private final String comment;
    private final ColumnValueTypeEnum type;
    private final boolean nullable;

    public TableColumn(String column, ColumnValueTypeEnum type) {
        this(column, type, true, "");
    }

    public TableColumn(String column, ColumnValueTypeEnum type, boolean nullable, String comment) {
        this.column = column;
        this.type = type;
        this.nullable = nullable;
        this.comment = Objects.requireNonNullElse(comment, "");
    }

    public String getColumn() {
        return column;
    }

    public String getComment() {
        return comment;
    }

    public ColumnValueTypeEnum getType() {
        return type;
    }

    public boolean isNullable() {
        return nullable;
    }


}
