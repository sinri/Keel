package io.github.sinri.keel.mysql.matrix;

import io.github.sinri.keel.core.json.JsonifiableEntity;
import io.github.sinri.keel.mysql.NamedMySQLConnection;
import io.github.sinri.keel.mysql.exception.KeelMySQLResultRowIndexError;
import io.github.sinri.keel.mysql.statement.AbstractReadStatement;
import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.sqlclient.SqlConnection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;

/**
 * @since 2.7
 */
public interface ResultRow extends JsonifiableEntity<ResultRow> {
    /**
     * @since 3.0.11
     * @since 3.0.18 Finished Technical Preview.
     */
    static <K, T extends ResultRow> Future<Map<K, List<T>>> fetchResultRowsToCategorizedMap(
            @NotNull NamedMySQLConnection namedSqlConnection,
            @NotNull AbstractReadStatement readStatement,
            @NotNull Class<T> classOfTableRow,
            @NotNull Function<T, K> categoryGenerator
    ) {
        return fetchResultRowsToCategorizedMap(namedSqlConnection.getSqlConnection(), readStatement, classOfTableRow, categoryGenerator);
    }

    /**
     * @since 3.0.11
     * @since 3.0.18 Finished Technical Preview.
     */
    static <K, T extends ResultRow> Future<Map<K, T>> fetchResultRowsToUniqueKeyBoundMap(
            @NotNull NamedMySQLConnection namedMySQLConnection,
            @NotNull AbstractReadStatement readStatement,
            @NotNull Class<T> classOfTableRow,
            @NotNull Function<T, K> uniqueKeyGenerator
    ) {
        return fetchResultRowsToUniqueKeyBoundMap(namedMySQLConnection.getSqlConnection(), readStatement, classOfTableRow, uniqueKeyGenerator);
    }

    /**
     * @since 3.0.11
     * @since 3.0.18 Finished Technical Preview.
     */
    static <T extends ResultRow> Future<List<T>> fetchResultRows(
            @NotNull NamedMySQLConnection namedMySQLConnection,
            @NotNull AbstractReadStatement readStatement,
            @NotNull Class<T> classOfTableRow
    ) {
        return fetchResultRows(namedMySQLConnection.getSqlConnection(), readStatement, classOfTableRow);
    }

    /**
     * @since 3.0.11
     * @since 3.0.18 Finished Technical Preview.
     */
    static <T extends ResultRow> Future<T> fetchResultRow(
            @NotNull NamedMySQLConnection namedMySQLConnection,
            @NotNull AbstractReadStatement readStatement,
            @NotNull Class<T> classOfTableRow
    ) {
        return fetchResultRow(namedMySQLConnection.getSqlConnection(), readStatement, classOfTableRow);
    }


    /**
     * @since 2.9.4
     */
    static <K, T extends ResultRow> Future<Map<K, List<T>>> fetchResultRowsToCategorizedMap(
            @NotNull SqlConnection sqlConnection,
            @NotNull AbstractReadStatement readStatement,
            @NotNull Class<T> classOfTableRow,
            @NotNull Function<T, K> categoryGenerator
    ) {
        Map<K, List<T>> map = new HashMap<>();
        return fetchResultRows(sqlConnection, readStatement, classOfTableRow)
                .compose(list -> {
                    list.forEach(item -> {
                        K category = categoryGenerator.apply(item);
                        map.computeIfAbsent(category, k -> new ArrayList<>()).add(item);
                    });
                    return Future.succeededFuture(map);
                });
    }

    /**
     * @since 2.9.4
     */
    static <K, T extends ResultRow> Future<Map<K, T>> fetchResultRowsToUniqueKeyBoundMap(
            @NotNull SqlConnection sqlConnection,
            @NotNull AbstractReadStatement readStatement,
            @NotNull Class<T> classOfTableRow,
            @NotNull Function<T, K> uniqueKeyGenerator
    ) {
        Map<K, T> map = new HashMap<>();
        return fetchResultRows(sqlConnection, readStatement, classOfTableRow)
                .compose(list -> {
                    list.forEach(item -> {
                        K uniqueKey = uniqueKeyGenerator.apply(item);
                        map.put(uniqueKey, item);
                    });
                    return Future.succeededFuture(map);
                });
    }

    /**
     * 在给定的SqlConnection上利用指定的AbstractReadStatement进行SQL查询（查询失败时异步报错）；
     * 尝试获取查询结果；
     * 如果存在，将所有行以classOfTableRow指定的类进行封装，异步返回此实例构成的List。
     */
    static <T extends ResultRow> Future<List<T>> fetchResultRows(
            @NotNull SqlConnection sqlConnection,
            @NotNull AbstractReadStatement readStatement,
            @NotNull Class<T> classOfTableRow
    ) {
        return readStatement.execute(sqlConnection)
                .compose(resultMatrix -> {
                    List<T> ts = resultMatrix.buildTableRowList(classOfTableRow);
                    return Future.succeededFuture(ts);
                });
    }

    /**
     * 在给定的SqlConnection上利用指定的AbstractReadStatement进行SQL查询（查询失败时异步报错）；
     * 尝试获取查询结果；
     * 如果不存在，异步返回null；
     * 如果存在，将第一行以classOfTableRow指定的类进行封装，异步返回此实例。
     */
    static <T extends ResultRow> Future<T> fetchResultRow(
            @NotNull SqlConnection sqlConnection,
            @NotNull AbstractReadStatement readStatement,
            @NotNull Class<T> classOfTableRow
    ) {
        return readStatement.execute(sqlConnection)
                .compose(resultMatrix -> {
                    try {
                        T t = resultMatrix.buildTableRowByIndex(0, classOfTableRow);
                        return Future.succeededFuture(t);
                    } catch (KeelMySQLResultRowIndexError e) {
                        return Future.succeededFuture(null);
                    }
                });
    }

    static JsonArray batchToJsonArray(@NotNull Collection<? extends ResultRow> rows) {
        JsonArray array = new JsonArray();
        rows.forEach(row -> array.add(row.getRow()));
        return array;
    }

    static JsonArray batchToJsonArray(@NotNull Collection<? extends ResultRow> rows, @NotNull Function<ResultRow, JsonObject> transformer) {
        JsonArray array = new JsonArray();
        rows.forEach(row -> array.add(transformer.apply(row)));
        return array;
    }

    default JsonObject getRow() {
        return toJsonObject();
    }

    /**
     * @since 2.8
     * @since 2.9.4 fix null field error
     */
    @Nullable
    default String readDateTime(@NotNull String field) {
        String s = readString(field);
        if (s == null) return null;
        return LocalDateTime.parse(s)
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    @Nullable
    default String readDate(@NotNull String field) {
        return readString(field);
    }

    /**
     * @since 2.9.4 fix null field error
     */
    @Nullable
    default String readTime(@NotNull String field) {
        var s = readString(field);
        if (s == null) return null;
        return s
                .replaceAll("[PTS]+", "")
                .replaceAll("[HM]", ":");
    }

    @Nullable
    default String readTimestamp(@NotNull String field) {
        return readDateTime(field);
    }
}
