package io.github.sinri.keel.mysql.statement;

import io.github.sinri.keel.mysql.MySQLExecutor;
import io.vertx.core.Future;
import io.vertx.sqlclient.SqlConnection;

import java.sql.SQLException;
import java.sql.Statement;

/**
 * @since 1.10
 */
public abstract class AbstractModifyStatement extends AbstractStatement {

    public int blockedExecuteForAffectedRows(Statement statement) throws SQLException {
        return blockedExecute(statement).getTotalAffectedRows();
    }

    public int blockedExecuteForAffectedRows() throws SQLException {
        return blockedExecute().getTotalAffectedRows();
    }

    public final int executeForAffectedRows() throws SQLException {
        return blockedExecuteForAffectedRows();
    }

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
                .compose(resultMatrix -> Future.succeededFuture(resultMatrix.getTotalAffectedRows()))
//                .recover(throwable -> {
//                    Keel.outputLogger("MySQL").warning(getClass().getName() + " executeForAffectedRows failed [" + throwable.getMessage() + "] when executing SQL: " + this);
//                    return Future.succeededFuture(-1);
//                })
                ;
    }

    /**
     * @return the MySQLExecutor for last inserted ID
     * @since 1.10
     */
    public MySQLExecutor<Integer> getExecutorForAffectedRows() {
        return MySQLExecutor.build(
                this::executeForAffectedRows,
                this::blockedExecuteForAffectedRows
        );
    }
}
