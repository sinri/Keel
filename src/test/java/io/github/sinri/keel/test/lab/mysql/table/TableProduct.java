package io.github.sinri.keel.test.lab.mysql.table;

import io.github.sinri.keel.mysql.matrix.ResultRow;
import io.vertx.core.json.JsonObject;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

public class TableProduct implements Table {
    public static final String TABLE = "product";
    public static final String ColumnNameProductID = "product_id";
    public static final String ColumnNameProductName = "product_name";
    public static final String ColumnNameProductLimit = "product_limit";
    public static final String ColumnNameProductPrice = "product_price";
    public static final TableColumn columnProductID = new TableColumn(ColumnNameProductID, ColumnValueTypeEnum.LONG_INTEGRAL);
    public static final TableColumn columnProductName = new TableColumn(ColumnNameProductName, ColumnValueTypeEnum.LITERAL);
    public static final TableColumn columnProductLimit = new TableColumn(ColumnNameProductLimit, ColumnValueTypeEnum.INTEGRAL);
    public static final TableColumn columnProductPrice = new TableColumn(ColumnNameProductPrice, ColumnValueTypeEnum.REAL);
    private static final TableProduct instance = new TableProduct();

    public static TableProduct getInstance() {
        return instance;
    }

    @Override
    public String tableName() {
        return TABLE;
    }

    @Override
    public List<TableColumn> columns() {
        return List.of(columnProductID, columnProductName, columnProductLimit, columnProductPrice);
    }

    public TableColumn getColumnProductID() {
        return columnProductID;
    }

    public TableColumn getColumnProductName() {
        return columnProductName;
    }

    public TableColumn getColumnProductLimit() {
        return columnProductLimit;
    }

    public TableColumn getColumnProductPrice() {
        return columnProductPrice;
    }

    public static class Row implements ResultRow {

        private JsonObject row;

        public Row(JsonObject tableRow) {
            this.row = tableRow;
        }

        public Long getProductId() {
            return readLong(ColumnNameProductID);
        }

        public String getProductName() {
            return readString(ColumnNameProductName);
        }

        public Integer getProductLimit() {
            return readInteger(ColumnNameProductLimit);
        }

        public Double getProductPrice() {
            return readDouble(ColumnNameProductPrice);
        }

        @NotNull
        @Override
        public JsonObject toJsonObject() {
            return this.row;
        }

        @NotNull
        @Override
        public ResultRow reloadDataFromJsonObject(JsonObject jsonObject) {
            this.row = Objects.requireNonNull(jsonObject);
            return this;
        }
    }
}
