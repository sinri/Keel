package io.github.sinri.keel.mysql.statement;

import io.github.sinri.keel.mysql.matrix.ResultRow;
import io.vertx.core.Future;
import io.vertx.sqlclient.SqlConnection;

import java.util.List;


/**
 * @since 1.10
 */
public abstract class AbstractReadStatement extends AbstractStatement {

    /**
     * @param sqlConnection SqlConnection
     * @param classT        class of type of result object
     * @param <T>           type of result object
     * @return result object, as AbstractRow
     * @since 2.1
     */
    public <T extends ResultRow> Future<T> queryForOneRow(SqlConnection sqlConnection, Class<T> classT) {
        return ResultRow.fetchResultRow(sqlConnection, this, classT);
        //return AbstractRow.fetchTableRow(sqlConnection, this, classT);
    }

    /**
     * @param sqlConnection SqlConnection
     * @param classT        class of type of result object
     * @param <T>           type of result object
     * @return result object, as AbstractRow
     * @since 2.1
     */
    public <T extends ResultRow> Future<List<T>> queryForRowList(SqlConnection sqlConnection, Class<T> classT) {
        return ResultRow.fetchResultRows(sqlConnection, this, classT);
        //return AbstractRow.fetchTableRowList(sqlConnection, this, classT);
    }
}
