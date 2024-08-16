package io.github.sinri.keel.mysql.statement;

import io.github.sinri.keel.mysql.NamedMySQLConnection;
import io.vertx.core.Future;
import io.vertx.sqlclient.SqlConnection;
import org.jetbrains.annotations.NotNull;


/**
 * @since 1.10
 */
public abstract class AbstractModifyStatement extends AbstractStatement {

    /**
     * @since 3.0.0
     */
    public static AbstractModifyStatement buildWithRawSQL(@NotNull String sql) {
        return new AbstractModifyStatement() {
            @Override
            public String toString() {
                return sql;
            }
        };
    }

    /**
     * @param sqlConnection get from pool
     * @return future with affected rows; failed future when failed
     * @since 1.7
     * @since 1.10 removed recover
     */
    public Future<Integer> executeForAffectedRows(@NotNull SqlConnection sqlConnection) {
        return execute(sqlConnection)
                .compose(resultMatrix -> {
                    var afx = resultMatrix.getTotalAffectedRows();
                    return Future.succeededFuture(afx);
                });
    }

    /**
     * @since 3.0.11
     * @since 3.0.18 Finished Technical Preview.
     */
    public Future<Integer> executeForAffectedRows(@NotNull NamedMySQLConnection namedMySQLConnection) {
        return executeForAffectedRows(namedMySQLConnection.getSqlConnection());
    }
}
