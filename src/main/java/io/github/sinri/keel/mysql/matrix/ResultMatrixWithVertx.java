package io.github.sinri.keel.mysql.matrix;

import io.github.sinri.keel.Keel;
import io.github.sinri.keel.mysql.exception.KeelSQLResultRowIndexError;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.mysqlclient.MySQLClient;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;
import io.vertx.sqlclient.data.Numeric;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

/**
 * @since 1.8
 */
public class ResultMatrixWithVertx implements ResultMatrix {
    private final RowSet<Row> rowSet;
    private final List<Row> rowList = new ArrayList<>();

    public ResultMatrixWithVertx(RowSet<Row> rowSet) {
        this.rowSet = rowSet;
        for (var row : rowSet) {
            rowList.add(row);
        }
    }

    public RowSet<Row> getRowSet() {
        return rowSet;
    }

    public int getTotalFetchedRows() {
        return rowSet.size();
    }

    public int getTotalAffectedRows() {
        return rowSet.rowCount();
    }

    public long getLastInsertedID() {
        return rowSet.property(MySQLClient.LAST_INSERTED_ID);
    }

    public JsonArray toJsonArray() {
        JsonArray array = new JsonArray();
        for (var row : rowList) {
            array.add(row.toJson());
        }
        return array;
    }

    public List<JsonObject> getRowList() {
        List<JsonObject> l = new ArrayList<>();
        for (var item : rowList) {
            l.add(item.toJson());
        }
        return l;
    }

    public JsonObject getFirstRow() throws KeelSQLResultRowIndexError {
        return getRowByIndex(0);
    }

    public JsonObject getRowByIndex(int index) throws KeelSQLResultRowIndexError {
        try {
            return rowList.get(index).toJson();
        } catch (IndexOutOfBoundsException indexOutOfBoundsException) {
            throw new KeelSQLResultRowIndexError(indexOutOfBoundsException);
        }
    }

    public <T extends ResultRow> T buildTableRowByIndex(int index, Class<T> classOfTableRow) throws KeelSQLResultRowIndexError {
        try {
            return ResultMatrix.buildTableRow(getRowByIndex(index), classOfTableRow);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                 NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    public <T extends ResultRow> List<T> buildTableRowList(Class<T> classOfTableRow) {
        try {
            return ResultMatrix.buildTableRowList(getRowList(), classOfTableRow);
        } catch (NoSuchMethodException | InvocationTargetException | InstantiationException |
                 IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @since 2.7
     */
    @Override
    public String getOneColumnOfFirstRowAsDateTime(String columnName) throws KeelSQLResultRowIndexError {
        return Keel.dateTimeHelper().getMySQLFormatLocalDateTimeExpression(getFirstRow().getString(columnName));
    }

    public String getOneColumnOfFirstRowAsString(String columnName) throws KeelSQLResultRowIndexError {
        return getFirstRow().getString(columnName);
    }

    public Numeric getOneColumnOfFirstRowAsNumeric(String columnName) throws KeelSQLResultRowIndexError {
        return Numeric.create(getFirstRow().getNumber(columnName));
    }

    public Integer getOneColumnOfFirstRowAsInteger(String columnName) throws KeelSQLResultRowIndexError {
        return getFirstRow().getInteger(columnName);
    }

    public Long getOneColumnOfFirstRowAsLong(String columnName) throws KeelSQLResultRowIndexError {
        return getFirstRow().getLong(columnName);
    }

    /**
     * @since 2.7
     */
    @Override
    public List<String> getOneColumnAsDateTime(String columnName) {
        List<String> x = new ArrayList<>();
        for (var row : rowList) {
            x.add(Keel.dateTimeHelper().getMySQLFormatLocalDateTimeExpression(row.getString(columnName)));
        }
        return x;
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
