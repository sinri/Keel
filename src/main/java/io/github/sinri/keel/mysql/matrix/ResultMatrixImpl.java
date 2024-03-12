package io.github.sinri.keel.mysql.matrix;

import io.github.sinri.keel.mysql.exception.KeelSQLResultRowIndexError;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.mysqlclient.MySQLClient;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;
import io.vertx.sqlclient.data.Numeric;

import javax.annotation.Nullable;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static io.github.sinri.keel.helper.KeelHelpersInterface.KeelHelpers;

/**
 * @since 1.8
 * @since 3.2.2 keep a list instance only, the row set instance would be ignored after extraction.
 */
class ResultMatrixImpl implements ResultMatrix {
    //private final RowSet<Row> rowSet;
    private final List<Row> rowList = new ArrayList<>();
    private final int totalFetchedRows;
    private final int totalAffectedRows;
    private final @Nullable Long lastInsertedID;

    public ResultMatrixImpl(RowSet<Row> rowSet) {
        //this.rowSet = rowSet;
        for (var row : rowSet) {
            rowList.add(row);
        }
        this.totalFetchedRows = rowSet.size();
        this.totalAffectedRows = rowSet.rowCount();
        this.lastInsertedID = rowSet.property(MySQLClient.LAST_INSERTED_ID);
    }

//    public RowSet<Row> getRowSet() {
//        return rowSet;
//    }

    @Override
    public int getTotalFetchedRows() {
        return totalFetchedRows;
    }

    @Override
    public int getTotalAffectedRows() {
        return totalAffectedRows;
    }

    @Override
    public long getLastInsertedID() {
        return Objects.requireNonNull(lastInsertedID);
    }

    @Override
    public JsonArray toJsonArray() {
        JsonArray array = new JsonArray();
        for (var row : rowList) {
            array.add(row.toJson());
        }
        return array;
    }

    @Override
    public List<JsonObject> getRowList() {
        List<JsonObject> l = new ArrayList<>();
        for (var item : rowList) {
            l.add(item.toJson());
        }
        return l;
    }

    @Override
    public JsonObject getFirstRow() throws KeelSQLResultRowIndexError {
        return getRowByIndex(0);
    }

    /**
     * @throws KeelSQLResultRowIndexError 行号不正确时抛出异常
     */
    @Override
    public JsonObject getRowByIndex(int index) throws KeelSQLResultRowIndexError {
        try {
            return rowList.get(index).toJson();
        } catch (IndexOutOfBoundsException indexOutOfBoundsException) {
            throw new KeelSQLResultRowIndexError(indexOutOfBoundsException);
        }
    }

    /**
     * @throws KeelSQLResultRowIndexError 行号不正确时抛出异常
     * @throws RuntimeException           封装类的时候可能会抛出异常
     */
    @Override
    public <T extends ResultRow> T buildTableRowByIndex(int index, Class<T> classOfTableRow) throws KeelSQLResultRowIndexError {
        try {
            return ResultMatrix.buildTableRow(getRowByIndex(index), classOfTableRow);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                 NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @throws RuntimeException 封装类的时候可能会抛出异常
     */
    @Override
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
        return KeelHelpers.datetimeHelper().getMySQLFormatLocalDateTimeExpression(getFirstRow().getString(columnName));
    }

    @Override
    public String getOneColumnOfFirstRowAsString(String columnName) throws KeelSQLResultRowIndexError {
        return getFirstRow().getString(columnName);
    }

    @Override
    public Numeric getOneColumnOfFirstRowAsNumeric(String columnName) throws KeelSQLResultRowIndexError {
        return Numeric.create(getFirstRow().getNumber(columnName));
    }

    @Override
    public Integer getOneColumnOfFirstRowAsInteger(String columnName) throws KeelSQLResultRowIndexError {
        return getFirstRow().getInteger(columnName);
    }

    @Override
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
            x.add(KeelHelpers.datetimeHelper().getMySQLFormatLocalDateTimeExpression(row.getString(columnName)));
        }
        return x;
    }

    @Override
    public List<String> getOneColumnAsString(String columnName) {
        List<String> x = new ArrayList<>();
        for (var row : rowList) {
            x.add(row.getString(columnName));
        }
        return x;
    }

    @Override
    public List<Numeric> getOneColumnAsNumeric(String columnName) {
        List<Numeric> x = new ArrayList<>();
        for (var row : rowList) {
            x.add(row.getNumeric(columnName));
        }
        return x;
    }

    @Override
    public List<Long> getOneColumnAsLong(String columnName) {
        List<Long> x = new ArrayList<>();
        for (var row : rowList) {
            x.add(row.getLong(columnName));
        }
        return x;
    }

    @Override
    public List<Integer> getOneColumnAsInteger(String columnName) {
        List<Integer> x = new ArrayList<>();
        for (var row : rowList) {
            x.add(row.getInteger(columnName));
        }
        return x;
    }
}
