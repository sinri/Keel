package io.github.sinri.keel.test.lab.mysql.table;

import io.github.sinri.keel.mysql.matrix.AbstractTableRow;
import io.vertx.core.json.JsonObject;
import org.jetbrains.annotations.Nullable;

public class TableB extends AbstractTableRow {
    public static final String TABLE = "table_b";
    public static final TableColumn column1 = new TableColumn("c1", ColumnValueTypeEnum.LONG_INTEGRAL);
    public static final TableColumn column2 = new TableColumn("c2", ColumnValueTypeEnum.LITERAL);
    public static final TableColumn column3 = new TableColumn("c3", ColumnValueTypeEnum.INTEGRAL);

    public TableB(@Nullable JsonObject tableRow) {
        super(tableRow);
    }


    @Override
    public String sourceTableName() {
        return TABLE;
    }

    public Long getC1() {
        return readLong(column1.getColumn());
    }

}
