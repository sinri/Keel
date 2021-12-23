package io.github.sinri.keel.mysql.matrix;


import io.vertx.core.json.JsonArray;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;
import io.vertx.sqlclient.data.Numeric;

import java.util.ArrayList;
import java.util.List;

/**
 * @since 1.1
 * May override this class to get Customized Data Matrix
 */
public class ResultMatrix {
    private final RowSet<Row> rowSet;
    private final List<Row> rowList = new ArrayList<>();

    public ResultMatrix(RowSet<Row> rowSet) {
        this.rowSet = rowSet;
        for (var row : rowSet) {
            rowList.add(row);
        }
    }

    public RowSet<Row> getRowSet() {
        return rowSet;
    }

    public JsonArray toJsonArray() {
        JsonArray array = new JsonArray();
        for (var row : rowList) {
            array.add(row.toJson());
        }
        return array;
    }

    public Row getFirstRow() {
        return rowList.get(0);
    }

    public String getOneColumnOfFirstRowAsString(String columnName) {
        return rowList.get(0).getString(columnName);
    }

    public Numeric getOneColumnOfFirstRowAsNumeric(String columnName) {
        return rowList.get(0).getNumeric(columnName);
    }

    public Integer getOneColumnOfFirstRowAsInteger(String columnName) {
        return rowList.get(0).getInteger(columnName);
    }

    public Long getOneColumnOfFirstRowAsLong(String columnName) {
        return rowList.get(0).getLong(columnName);
    }

    public List<String> getOneColumnAsString(String columnName) {
        List<String> x = new ArrayList<>();
        for (var row : rowList) {
            x.add(row.getString(columnName));
        }
        return x;
    }

    public List<Numeric> getOneColumnAsNumeric(String columnName) {
        List<Numeric> x = new ArrayList<>();
        for (var row : rowList) {
            x.add(row.getNumeric(columnName));
        }
        return x;
    }

    public List<Long> getOneColumnAsLong(String columnName) {
        List<Long> x = new ArrayList<>();
        for (var row : rowList) {
            x.add(row.getLong(columnName));
        }
        return x;
    }

    public List<Integer> getOneColumnAsInteger(String columnName) {
        List<Integer> x = new ArrayList<>();
        for (var row : rowList) {
            x.add(row.getInteger(columnName));
        }
        return x;
    }
}
