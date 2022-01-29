package io.github.sinri.keel.mysql.statement;

import io.github.sinri.keel.mysql.matrix.ResultMatrix;
import io.github.sinri.keel.mysql.matrix.ResultMatrixWithVertx;
import io.vertx.core.Future;
import io.vertx.sqlclient.SqlConnection;

/**
 * @since 1.7
 */
abstract public class AbstractStatement {
    /**
     * @return The SQL Generated
     */
    public abstract String toString();

    public static String SQL_COMPONENT_SEPARATOR = " ";//"\n";

    /**
     * @param sqlConnection Fetched from Pool
     * @return the result matrix wrapped in a future, any error would cause a failed future
     */
    public final Future<ResultMatrix> execute(SqlConnection sqlConnection) {
        return sqlConnection.preparedQuery(this.toString())
                .execute()
                .compose(rows -> Future.succeededFuture(new ResultMatrixWithVertx(rows)));
    }
}
