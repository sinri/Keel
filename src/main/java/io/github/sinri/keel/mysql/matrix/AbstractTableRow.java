package io.github.sinri.keel.mysql.matrix;

import io.github.sinri.keel.mysql.MySQLExecutor;
import io.github.sinri.keel.mysql.exception.KeelSQLResultRowIndexError;
import io.github.sinri.keel.mysql.statement.AbstractReadStatement;
import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;

/**
 * @since 1.10 Designed for a wrapper of each row in ResultMatrix
 */
public abstract class AbstractTableRow {
    private final JsonObject tableRow;


    public AbstractTableRow(JsonObject tableRow) {
        this.tableRow = tableRow;
    }

    public static <T extends AbstractTableRow> MySQLExecutor<List<T>> buildTableRowListFetcher(
            AbstractReadStatement readStatement,
            Class<T> classOfTableRow
    ) {
        return MySQLExecutor.build(
                sqlConnection -> readStatement.execute(sqlConnection)
                        .compose(resultMatrix -> Future.succeededFuture(resultMatrix.buildTableRowList(classOfTableRow))),
                statement -> readStatement.blockedExecute(statement).buildTableRowList(classOfTableRow)
        );
    }

    public static <T extends AbstractTableRow> MySQLExecutor<T> buildTableRowFetcher(
            AbstractReadStatement readStatement,
            Class<T> classOfTableRow
    ) {
        return MySQLExecutor.build(
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

    public JsonObject getTableRow() {
        return tableRow;
    }

    public String getFieldAsString(String field) {
        return tableRow.getString(field);
    }

    public Number getFieldAsNumber(String field) {
        return tableRow.getNumber(field);
    }

    /**
     * @param rows collection of AbstractTableRow
     * @return a json array
     * @since 1.13
     */
    public static JsonArray rowsToJsonArray(Collection<? extends AbstractTableRow> rows) {
        JsonArray array = new JsonArray();
        rows.forEach(row -> array.add(row.getTableRow()));
        return array;
    }

    /**
     * @param rows        collection of AbstractTableRow
     * @param transformer a function, AbstractTableRow->JsonObject
     * @return a json array
     * @since 1.13
     */
    public static JsonArray rowsToJsonArray(Collection<? extends AbstractTableRow> rows, Function<AbstractTableRow, JsonObject> transformer) {
        JsonArray array = new JsonArray();
        rows.forEach(row -> array.add(transformer.apply(row)));
        return array;
    }
}
