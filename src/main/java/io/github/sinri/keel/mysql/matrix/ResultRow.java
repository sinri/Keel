package io.github.sinri.keel.mysql.matrix;

import io.github.sinri.keel.Keel;
import io.github.sinri.keel.core.json.JsonifiableEntity;
import io.github.sinri.keel.mysql.exception.KeelSQLResultRowIndexError;
import io.github.sinri.keel.mysql.statement.AbstractReadStatement;
import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.sqlclient.SqlConnection;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

/**
 * @since 2.7
 */
public interface ResultRow extends JsonifiableEntity<ResultRow> {
    static <T extends ResultRow> Future<List<T>> fetchResultRows(
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

    static <T extends ResultRow> Future<T> fetchResultRow(
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

    static JsonArray batchToJsonArray(Collection<? extends ResultRow> rows) {
        JsonArray array = new JsonArray();
        rows.forEach(row -> array.add(row.getRow()));
        return array;
    }

    static JsonArray batchToJsonArray(Collection<? extends ResultRow> rows, Function<ResultRow, JsonObject> transformer) {
        JsonArray array = new JsonArray();
        rows.forEach(row -> array.add(transformer.apply(row)));
        return array;
    }

    default JsonObject getRow() {
        return toJsonObject();
    }

    @Deprecated(since = "2.8")
    default String getFieldAsString(String field) {
        return readString(field);
    }

    @Deprecated(since = "2.8")
    default Number getFieldAsNumber(String field) {
        return readNumber(field);
    }

    @Deprecated(since = "2.8")
    default String getFieldAsDateTime(String filed) {
        return Keel.dateTimeHelper().getMySQLFormatLocalDateTimeExpression(getRow().getString(filed));
    }

    /**
     * @since 2.8
     */
    default String readDateTime(String field) {
        return LocalDateTime.parse(readString(field))
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    default String readDate(String field) {
        return readString(field);
    }

    default String readTime(String field) {
        return readString(field)
                .replaceAll("[PTS]+", "")
                .replaceAll("[HM]", ":");
    }

    default String readTimestamp(String field) {
        return readDateTime(field);
    }
}
