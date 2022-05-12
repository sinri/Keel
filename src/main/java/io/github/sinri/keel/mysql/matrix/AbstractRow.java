package io.github.sinri.keel.mysql.matrix;

import io.github.sinri.keel.mysql.DuplexExecutorForMySQL;
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
 */
public abstract class AbstractRow {
    private final JsonObject row;

    public AbstractRow(JsonObject tableRow) {
        this.row = tableRow;
    }

    /**
     * @param sqlConnection
     * @param readStatement
     * @param classOfTableRow
     * @param <T>
     * @return
     * @since 2.1
     */
    public static <T extends AbstractRow> Future<List<T>> fetchTableRowList(
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
     * @param sqlConnection
     * @param readStatement
     * @param classOfTableRow
     * @param <T>
     * @return
     * @since 2.1
     */
    public static <T extends AbstractRow> Future<T> fetchTableRow(
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

    @Deprecated
    public static <T extends AbstractRow> DuplexExecutorForMySQL<List<T>> buildTableRowListFetcher(
            AbstractReadStatement readStatement,
            Class<T> classOfTableRow
    ) {
        return new DuplexExecutorForMySQL<>(
                sqlConnection -> readStatement.execute(sqlConnection)
                        .compose(resultMatrix -> Future.succeededFuture(resultMatrix.buildTableRowList(classOfTableRow))),
                statement -> readStatement.blockedExecute(statement).buildTableRowList(classOfTableRow)
        );
    }

    @Deprecated
    public static <T extends AbstractRow> DuplexExecutorForMySQL<T> buildTableRowFetcher(
            AbstractReadStatement readStatement,
            Class<T> classOfTableRow
    ) {
        return new DuplexExecutorForMySQL<>(
                sqlConnection -> readStatement.execute(sqlConnection)
                        .compose(resultMatrix -> {
                            T t;
                            try {
                                t = resultMatrix.buildTableRowByIndex(0, classOfTableRow);
                            } catch (KeelSQLResultRowIndexError e) {
                                return Future.succeededFuture(null);
                            }
                            return Future.succeededFuture(t);
                        }),
                statement -> {
                    try {
                        return readStatement.blockedExecute(statement).buildTableRowByIndex(0, classOfTableRow);
                    } catch (KeelSQLResultRowIndexError e) {
                        return null;
                    }
                }
        );
    }

    /**
     * @param rows collection of AbstractTableRow
     * @return a json array
     * @since 1.13
     */
    public static JsonArray rowsToJsonArray(Collection<? extends AbstractRow> rows) {
        JsonArray array = new JsonArray();
        rows.forEach(row -> array.add(row.getRow()));
        return array;
    }

    /**
     * @param rows        collection of AbstractTableRow
     * @param transformer a function, AbstractTableRow->JsonObject
     * @return a json array
     * @since 1.13
     */
    public static JsonArray rowsToJsonArray(Collection<? extends AbstractRow> rows, Function<AbstractRow, JsonObject> transformer) {
        JsonArray array = new JsonArray();
        rows.forEach(row -> array.add(transformer.apply(row)));
        return array;
    }

    public JsonObject getRow() {
        return row;
    }

    public String getFieldAsString(String field) {
        return row.getString(field);
    }

    public Number getFieldAsNumber(String field) {
        return row.getNumber(field);
    }


}
