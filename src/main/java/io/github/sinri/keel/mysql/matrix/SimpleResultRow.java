package io.github.sinri.keel.mysql.matrix;

import io.github.sinri.keel.mysql.exception.KeelSQLResultRowIndexError;
import io.github.sinri.keel.mysql.statement.AbstractReadStatement;
import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.sqlclient.SqlConnection;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;

/**
 * @since 1.10 Designed for a wrapper of each row in ResultMatrix
 * @since 2.0 renamed from AbstractTableRow
 * @since 2.7 renamed from AbstractRow
 */
public class SimpleResultRow implements ResultRow {
    private JsonObject row;

    public SimpleResultRow(JsonObject tableRow) {
        this.reloadDataFromJsonObject(tableRow);
    }

    /**
     * @see ResultRow#fetchResultRows(SqlConnection, AbstractReadStatement, Class)
     * @since 2.1
     * @deprecated
     */
    @Deprecated(since = "2.7", forRemoval = true)
    public static <T extends SimpleResultRow> Future<List<T>> fetchTableRowList(
            SqlConnection sqlConnection,
            AbstractReadStatement readStatement,
            Class<T> classOfTableRow
    ) {
        return readStatement.execute(sqlConnection)
                .compose(resultMatrix -> {
                    List<T> ts = resultMatrix.buildTableRowList(classOfTableRow);
                    return Future.succeededFuture(ts);
                });
    }

    /**
     * @see ResultRow#fetchResultRow(SqlConnection, AbstractReadStatement, Class)
     * @since 2.1
     * @deprecated
     */
    @Deprecated(since = "2.7", forRemoval = true)
    public static <T extends SimpleResultRow> Future<T> fetchTableRow(
            SqlConnection sqlConnection,
            AbstractReadStatement readStatement,
            Class<T> classOfTableRow
    ) {
        return readStatement.execute(sqlConnection)
                .compose(resultMatrix -> {
                    T t;
                    try {
                        t = resultMatrix.buildTableRowByIndex(0, classOfTableRow);
                    } catch (KeelSQLResultRowIndexError e) {
                        return Future.succeededFuture(null);
                    }
                    return Future.succeededFuture(t);
                });
    }

    /**
     * @param rows collection of AbstractTableRow
     * @return a json array
     * @see ResultRow#batchToJsonArray(Collection)
     * @since 1.13
     * @deprecated
     */
    @Deprecated(since = "2.7", forRemoval = true)
    public static JsonArray rowsToJsonArray(Collection<? extends SimpleResultRow> rows) {
        JsonArray array = new JsonArray();
        rows.forEach(row -> array.add(row.getRow()));
        return array;
    }

    /**
     * @param rows        collection of AbstractTableRow
     * @param transformer a function, AbstractTableRow→JsonObject
     * @return a json array
     * @see ResultRow#batchToJsonArray(Collection, Function)
     * @since 1.13
     * @deprecated
     */
    @Deprecated(since = "2.7", forRemoval = true)
    public static JsonArray rowsToJsonArray(Collection<? extends SimpleResultRow> rows, Function<SimpleResultRow, JsonObject> transformer) {
        JsonArray array = new JsonArray();
        rows.forEach(row -> array.add(transformer.apply(row)));
        return array;
    }

    @Override
    public final JsonObject toJsonObject() {
        return row;
    }

    @Override
    public final ResultRow reloadDataFromJsonObject(JsonObject jsonObject) {
        this.row = jsonObject;
        return this;
    }
}
