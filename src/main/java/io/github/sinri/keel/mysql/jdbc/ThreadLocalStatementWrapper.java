package io.github.sinri.keel.mysql.jdbc;

import io.github.sinri.keel.mysql.matrix.ResultMatrix;

import java.sql.SQLException;
import java.sql.Statement;

/**
 * @since 1.9
 * @since 2.0 由于 VERTICLE 的发现，应用场景减少。
 * @deprecated since 2.1
 */
@Deprecated
public class ThreadLocalStatementWrapper {

    private final ThreadLocal<Statement> threadLocalStatement;
    private final KeelJDBCForMySQL jdbc;

    public ThreadLocalStatementWrapper(KeelJDBCForMySQL jdbc) {
        this.jdbc = jdbc;
        this.threadLocalStatement = new ThreadLocal<>();
    }

    public static <T> T runWithTransactionExecutor(ThreadLocalStatementWrapper threadLocalStatementWrapper, TransactionExecutor<T> transactionExecutor) throws SQLException {
        threadLocalStatementWrapper.startNewTransactionInThreadLocalStatement();
        T result = null;
        boolean shouldCommit = true;
        Exception errorInTransaction = null;
        try {
            result = transactionExecutor.execute();
        } catch (Exception e) {
            shouldCommit = false;
            errorInTransaction = e;
        }
        if (shouldCommit) {
            threadLocalStatementWrapper.commitInThreadLocalStatement();
            return result;
        } else {
            threadLocalStatementWrapper.rollbackInThreadLocalStatement();
            throw new SQLException("Rollback Done, due to " + errorInTransaction.getMessage(), errorInTransaction);
        }
    }

    public Statement getCurrentThreadLocalStatement() {
        Statement statement = threadLocalStatement.get();
        if (statement == null) {
            // 默认生成一个非事务的连接
            try {
                statement = jdbc.createStatement(true);
            } catch (SQLException e) {
                throw new RuntimeException("CANNOT OBTAIN JDBC STATEMENT: " + e.getMessage(), e);
            }
            threadLocalStatement.set(statement);
        }
        return statement;
    }

    /**
     * @throws SQLException
     * @since 1.9
     */
    public void startNewTransactionInThreadLocalStatement() throws SQLException {
        Statement statement = threadLocalStatement.get();
        if (statement != null) {
            statement.close();
            threadLocalStatement.remove();
        }
        threadLocalStatement.set(jdbc.createStatement(false));
    }

    public void commitInThreadLocalStatement() throws SQLException {
        Statement statement = threadLocalStatement.get();
        if (statement != null) {
            KeelJDBCForMySQL.commit(statement);
            statement.close();
            threadLocalStatement.remove();
        }
    }

    public void rollbackInThreadLocalStatement() throws SQLException {
        Statement statement = threadLocalStatement.get();
        if (statement != null) {
            KeelJDBCForMySQL.rollback(statement);
            statement.close();
            threadLocalStatement.remove();
        }
    }

    public ResultMatrix queryForSelection(String sql) throws SQLException {
        return KeelJDBCForMySQL.queryForSelection(sql, getCurrentThreadLocalStatement());
    }

    public ResultMatrix executeForInsertion(String sql) throws SQLException {
        return KeelJDBCForMySQL.executeForInsertion(sql, getCurrentThreadLocalStatement());
    }

    public ResultMatrix executeForModification(String sql) throws SQLException {
        return KeelJDBCForMySQL.executeForModification(sql, getCurrentThreadLocalStatement());
    }
}
