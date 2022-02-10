package io.github.sinri.keel.mysql.statement;

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

    public long blockedExecuteForLastInsertedID(Statement statement) throws SQLException {
        return blockedExecute(statement).getLastInsertedID();
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
     * @param sqlConnection get from pool
     * @return future with last inserted id; if any error occurs, failed future returned instead.
     * @since 1.7
     * @since 1.10, removed the recover block
     */
    public Future<Long> executeForLastInsertedID(SqlConnection sqlConnection) {
        return execute(sqlConnection)
                .compose(resultMatrix -> Future.succeededFuture(resultMatrix.getLastInsertedID()))
//                .recover(throwable -> {
//                    Keel.outputLogger("MySQL").warning(getClass().getName() + " executeForLastInsertedID failed [" + throwable.getMessage() + "] when executing SQL: " + this);
//                    return Future.succeededFuture(-1L);
//                })
                ;
    }
}
