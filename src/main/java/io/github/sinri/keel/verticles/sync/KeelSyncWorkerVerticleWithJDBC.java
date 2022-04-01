package io.github.sinri.keel.verticles.sync;

import io.github.sinri.keel.Keel;
import io.github.sinri.keel.mysql.jdbc.ThreadLocalStatementWrapper;
import io.github.sinri.keel.mysql.jdbc.TransactionExecutor;

import java.sql.SQLException;

/**
 * @param <R>
 * @since 1.14
 */
@Deprecated
abstract public class KeelSyncWorkerVerticleWithJDBC<R> extends KeelSyncWorkerVerticle<R> {
    @Override
    protected R syncExecute() throws SQLException {
        return ThreadLocalStatementWrapper.runWithTransactionExecutor(
                Keel.getMySQLKitWithJDBC().getThreadLocalStatementWrapper(),
                new TransactionExecutor<>() {
                    @Override
                    public R execute() throws Exception {
                        return transactionBody();
                    }
                }
        );
    }

    abstract protected R transactionBody() throws Exception;
}
