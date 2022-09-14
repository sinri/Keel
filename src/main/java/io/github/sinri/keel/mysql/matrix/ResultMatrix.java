package io.github.sinri.keel.mysql.matrix;


import io.github.sinri.keel.mysql.exception.KeelSQLResultRowIndexError;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;
import io.vertx.sqlclient.data.Numeric;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

/**
 * @since 1.1
 * @since 1.8 becomes interface
 * May overrides this class to get Customized Data Matrix
 */
public interface ResultMatrix {

    /**
     * @since 1.10
     */
    static <T extends ResultRow> T buildTableRow(JsonObject row, Class<T> classOfTableRow) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        return classOfTableRow.getConstructor(JsonObject.class).newInstance(row);
    }

    /**
     * @since 1.10
     */
    static <T extends ResultRow> List<T> buildTableRowList(List<JsonObject> rowList, Class<T> classOfTableRow) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        ArrayList<T> list = new ArrayList<>();
        for (var x : rowList) {
            list.add(ResultMatrix.buildTableRow(x, classOfTableRow));
        }
        return list;
    }

    /**
     * @since 2.8
     */
    static ResultMatrix create(RowSet<Row> rowSet) {
        return new ResultMatrixWithVertx(rowSet);
    }

    List<JsonObject> getRowList();

    int getTotalFetchedRows();

    int getTotalAffectedRows();

    long getLastInsertedID();

    JsonArray toJsonArray();

    JsonObject getFirstRow() throws KeelSQLResultRowIndexError;

    JsonObject getRowByIndex(int index) throws KeelSQLResultRowIndexError;

    /**
     * @since 1.10
     */
    <T extends ResultRow> T buildTableRowByIndex(int index, Class<T> classOfTableRow) throws KeelSQLResultRowIndexError;

    String getOneColumnOfFirstRowAsDateTime(String columnName) throws KeelSQLResultRowIndexError;

    String getOneColumnOfFirstRowAsString(String columnName) throws KeelSQLResultRowIndexError;

    Numeric getOneColumnOfFirstRowAsNumeric(String columnName) throws KeelSQLResultRowIndexError;

    Integer getOneColumnOfFirstRowAsInteger(String columnName) throws KeelSQLResultRowIndexError;

    Long getOneColumnOfFirstRowAsLong(String columnName) throws KeelSQLResultRowIndexError;

    List<String> getOneColumnAsDateTime(String columnName);

    List<String> getOneColumnAsString(String columnName);

    List<Numeric> getOneColumnAsNumeric(String columnName);

    List<Long> getOneColumnAsLong(String columnName);

    List<Integer> getOneColumnAsInteger(String columnName);

    /**
     * @throws RuntimeException 封装类的时候可能会抛出异常
     * @since 1.10
     */
    <T extends ResultRow> List<T> buildTableRowList(Class<T> classOfTableRow);
}
