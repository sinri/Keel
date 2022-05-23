package io.github.sinri.keel.mysql.statement;

import io.vertx.core.Future;
import io.vertx.sqlclient.SqlConnection;

/**
 * @since 1.10
 */
public abstract class AbstractModifyStatement extends AbstractStatement {

    /**
     * @param sqlConnection get from pool
     * @return future with affected rows; failed future when failed
     * @since 1.7
     * @since 1.10 removed recover
     */
    public Future<Integer> executeForAffectedRows(SqlConnection sqlConnection) {
        return execute(sqlConnection)
                .compose(resultMatrix -> Future.succeededFuture(resultMatrix.getTotalAffectedRows()));
    }
}
