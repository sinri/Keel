package io.github.sinri.keel.mysql.matrix;


import io.github.sinri.keel.mysql.exception.KeelSQLResultRowIndexError;
import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;
import io.vertx.sqlclient.data.Numeric;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;

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
        return new ResultMatrixImpl(rowSet);
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

    /**
     * 
     * 
     * 
     * @since 2.9.4
     */
    default <K> Future<Map<K, List<JsonObject>>> buildCategorizedRowsMap(Function<JsonObject, K> categoryGenerator) {
        Map<K, List<JsonObject>> map = new HashMap<>();
        var list = getRowList();
        list.forEach(item -> {
            K category = categoryGenerator.apply(item);
            map.computeIfAbsent(category, k -> new ArrayList<>()).add(item);
        });
        return Future.succeededFuture(map);
    }

    /**
     * 
     * 
     * 
     * @since 2.9.4
     */
    default <K> Future<Map<K, JsonObject>> buildUniqueKeyBoundRowMap(Function<JsonObject, K> uniqueKeyGenerator) {
        Map<K, JsonObject> map = new HashMap<>();
        var list = getRowList();
        list.forEach(item -> {
            K uniqueKey = uniqueKeyGenerator.apply(item);
            map.put(uniqueKey, item);
        });
        return Future.succeededFuture(map);
    }

    /**
     * Categorized Rows Map, i.e. category mapping to a list of rows.
     *
     * 
     * 
     * 
     * 
     * 
     * @since 2.9.4
     */
    default <K, T extends ResultRow> Future<Map<K, List<T>>> buildCategorizedRowsMap(Class<T> classOfTableRow, Function<T, K> categoryGenerator) {
        Map<K, List<T>> map = new HashMap<>();
        var list = buildTableRowList(classOfTableRow);
        list.forEach(item -> {
            K category = categoryGenerator.apply(item);
            map.computeIfAbsent(category, k -> new ArrayList<>()).add(item);
        });
        return Future.succeededFuture(map);
    }

    /**
     * Unique key bound rows map, i.e. One unique Key mapping to one result row.
     * WARNING: if the uniqueKeyGenerator provides duplicated key, the mapped value would be uncertainly single.
     *
     * 
     * 
     * 
     * 
     * 
     */
    default <K, T extends ResultRow> Future<Map<K, T>> buildUniqueKeyBoundRowMap(Class<T> classOfTableRow, Function<T, K> uniqueKeyGenerator) {
        Map<K, T> map = new HashMap<>();
        var list = buildTableRowList(classOfTableRow);
        list.forEach(item -> {
            K category = uniqueKeyGenerator.apply(item);
            map.put(category, item);
        });
        return Future.succeededFuture(map);
    }

    /**
     * 类似矩阵转置的玩意。
     *
     * 
     * 
     * 
     * 
     * @since 2.9.4
     */
    default <K, V> Future<Map<K, V>> buildCustomizedMap(
            BiConsumer<Map<K, V>, JsonObject> rowToMapHandler
    ) {
        Map<K, V> map = new HashMap<>();
        var list = getRowList();
        list.forEach(item -> {
            rowToMapHandler.accept(map, item);
        });
        return Future.succeededFuture(map);
    }
}
