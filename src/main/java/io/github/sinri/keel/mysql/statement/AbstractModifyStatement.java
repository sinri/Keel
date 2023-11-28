package io.github.sinri.keel.mysql.statement;

import io.github.sinri.keel.core.TechnicalPreview;
import io.github.sinri.keel.mysql.NamedMySQLConnection;
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
                .compose(resultMatrix -> {
                    var afx = resultMatrix.getTotalAffectedRows();
                    return Future.succeededFuture(afx);
                });
    }

    @TechnicalPreview(since = "3.0.11")
    public Future<Integer> executeForAffectedRows(NamedMySQLConnection namedMySQLConnection) {
        return executeForAffectedRows(namedMySQLConnection.getSqlConnection());
    }

    /**
     * @since 3.0.0
     */
    public static AbstractModifyStatement buildWithRawSQL(String sql) {
        return new AbstractModifyStatement() {
            @Override
            public String toString() {
                return sql;
            }
        };
    }
}
