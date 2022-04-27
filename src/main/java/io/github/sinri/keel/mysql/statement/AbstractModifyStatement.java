package io.github.sinri.keel.mysql.statement;

import io.github.sinri.keel.mysql.DuplexExecutorForMySQL;
import io.vertx.core.Future;
import io.vertx.sqlclient.SqlConnection;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.function.Function;

/**
 * @since 1.10
 */
public abstract class AbstractModifyStatement extends AbstractStatement {

    @Deprecated
    public int blockedExecuteForAffectedRows(Statement statement) throws SQLException {
        return blockedExecute(statement).getTotalAffectedRows();
    }

    @Deprecated
    public int blockedExecuteForAffectedRows() throws SQLException {
        return blockedExecute().getTotalAffectedRows();
    }

    @Deprecated
    public final int executeForAffectedRows() throws SQLException {
        return blockedExecuteForAffectedRows();
    }

    @Deprecated
    public final int executeForAffectedRows(Statement statement) throws SQLException {
        return blockedExecuteForAffectedRows(statement);
    }

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

    /**
     * @return
     * @since 2.0
     * @deprecated since 2.1
     */
    @Deprecated
    public DuplexExecutorForMySQL<Integer> getExecutorForAffectedRows() {
        return new DuplexExecutorForMySQL<>(
                this::executeForAffectedRows,
                this::blockedExecuteForAffectedRows
        );
    }

    /**
     * @param afxChecker
     * @param recoveredValue
     * @param <R>
     * @return
     * @since 2.0
     * @deprecated since 2.1
     */
    @Deprecated
    public <R> DuplexExecutorForMySQL<R> getExecutorForAffectedRows(Function<Integer, R> afxChecker, R recoveredValue) {
        return new DuplexExecutorForMySQL<>(
                sqlConnection -> executeForAffectedRows(sqlConnection)
                        .compose(afx -> Future.succeededFuture(afxChecker.apply(afx)))
                        .recover(throwable -> Future.succeededFuture(recoveredValue)),
                statement -> {
                    try {
                        int afx = blockedExecuteForAffectedRows(statement);
                        return afxChecker.apply(afx);
                    } catch (SQLException sqlException) {
                        return recoveredValue;
                    }
                }
        );
    }
}
