package io.github.sinri.keel.mysql.matrix;

import io.github.sinri.keel.mysql.MySQLExecutor;
import io.github.sinri.keel.mysql.exception.KeelSQLResultRowIndexError;
import io.github.sinri.keel.mysql.statement.AbstractReadStatement;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;

import java.sql.SQLException;
import java.util.List;

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
                                return Future.failedFuture(e);
                            }
                            return Future.succeededFuture(t);
                        }),
                statement -> {
                    try {
                        return readStatement.blockedExecute(statement).buildTableRowByIndex(0, classOfTableRow);
                    } catch (KeelSQLResultRowIndexError e) {
                        throw new SQLException(e);
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
}
