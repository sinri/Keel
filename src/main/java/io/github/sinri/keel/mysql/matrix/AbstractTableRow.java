package io.github.sinri.keel.mysql.matrix;

import io.github.sinri.keel.core.DuplexExecutor;
import io.github.sinri.keel.mysql.statement.AbstractReadStatement;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.sqlclient.SqlConnection;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

/**
 * @since 1.10 Designed for a wrapper of each row in ResultMatrix
 */
public abstract class AbstractTableRow {
    private final JsonObject tableRow;


    public AbstractTableRow(JsonObject tableRow) {
        this.tableRow = tableRow;
    }

    public static <T extends AbstractTableRow> DuplexExecutor<List<T>> buildTableRowListWithReadStatement(
            AbstractReadStatement readStatement,
            SqlConnection sqlConnection,
            Statement statement,
            Class<T> classOfTableRow
    ) {
        return DuplexExecutor.build(
                asyncVoid -> readStatement.execute(sqlConnection)
                        .compose(resultMatrix -> Future.succeededFuture(resultMatrix.buildTableRowList(classOfTableRow))),
                listSyncExecuteResult -> {
                    try {
                        listSyncExecuteResult.setResult(readStatement.blockedExecute(statement).buildTableRowList(classOfTableRow));
                    } catch (SQLException e) {
                        listSyncExecuteResult.setError(e);
                    }
                    return listSyncExecuteResult;
                }
        );
    }

    public static <T extends AbstractTableRow> DuplexExecutor<T> buildTableRowWithReadStatement(
            AbstractReadStatement readStatement,
            SqlConnection sqlConnection,
            Statement statement,
            Class<T> classOfTableRow
    ) {
        return DuplexExecutor.build(
                asyncVoid -> readStatement.execute(sqlConnection)
                        .compose(resultMatrix -> Future.succeededFuture(resultMatrix.buildTableRowByIndex(0, classOfTableRow))),
                listSyncExecuteResult -> {
                    try {
                        listSyncExecuteResult.setResult(readStatement.blockedExecute(statement).buildTableRowByIndex(0, classOfTableRow));
                    } catch (SQLException e) {
                        listSyncExecuteResult.setError(e);
                    }
                    return listSyncExecuteResult;
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
