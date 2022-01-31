package io.github.sinri.keel.mysql.jdbc;

import io.github.sinri.keel.Keel;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.function.Function;

/**
 * @param <T>
 * @since 1.8
 */
public class KeelJDBCForMySQLTransaction<T> {
    private final Connection connection;
    private final Statement statement;
    private final Function<KeelJDBCForMySQLTransaction<T>, Boolean> transactionBody;
    private final TransactionExecutor<T> transactionExecutor;

    private boolean didCommit = false;
    private boolean didRollback = false;
    private T result = null;
    private Throwable rollbackCause = null;

    /**
     * @param jdbc            the source JDBC
     * @param transactionBody if you want commit it, return true; or false.
     * @throws SQLException If connection cannot be obtained.
     */
    public KeelJDBCForMySQLTransaction(
            KeelJDBCForMySQL jdbc,
            Function<KeelJDBCForMySQLTransaction<T>, Boolean> transactionBody
    ) throws SQLException {
        this.statement = jdbc.createStatement(false);
        this.connection = statement.getConnection();

        this.transactionBody = transactionBody;
        transactionExecutor = null;

        this.execute_for_func();
        this.close();
    }

    public KeelJDBCForMySQLTransaction(
            KeelJDBCForMySQL jdbc,
            TransactionExecutor<T> transactionExecutor
    ) throws SQLException {
        this.statement = jdbc.createStatement(false);
        this.connection = statement.getConnection();

        transactionBody = null;
        this.transactionExecutor = transactionExecutor;

        this.execute_for_executor();
        this.close();
    }

    public boolean isDidCommit() {
        return didCommit;
    }

    public boolean isDidRollback() {
        return didRollback;
    }

    public T getResult() {
        return result;
    }

    public void setResult(T result) {
        this.result = result;
    }

    public Throwable getRollbackCause() {
        return rollbackCause;
    }

    public void setRollbackCause(Throwable rollbackCause) {
        this.rollbackCause = rollbackCause;
    }

    /**
     *
     */
    protected void execute_for_func() {
        Boolean completed = transactionBody.apply(this);
        try {
            if (completed) {
                connection.commit();
                this.didCommit = true;
            } else {
                connection.rollback();
                this.didRollback = true;
            }
        } catch (SQLException e) {
            Keel.logger("JDBC").exception(e);
        }
    }

    protected void execute_for_executor() {
        boolean completed;
        T result = null;
        try {
            result = transactionExecutor.execute();
            completed = true;
        } catch (Exception e) {
            completed = false;
            this.rollbackCause = e;
        }
        try {
            if (completed) {
                connection.commit();
                this.didCommit = true;
                this.result = result;
            } else {
                connection.rollback();
                this.didRollback = true;
            }
        } catch (SQLException e) {
            Keel.logger("JDBC").exception(e);
        }
    }

    protected void close() throws SQLException {
        if (statement != null) {
            statement.close();
        }
        if (connection != null) {
            connection.close();
        }
    }

}
