package io.github.sinri.keel.mysql.jdbc;

import io.github.sinri.keel.mysql.KeelMySQLOptions;
import io.github.sinri.keel.mysql.matrix.ResultMatrix;
import io.github.sinri.keel.mysql.matrix.ResultMatrixWithJDBC;

import java.sql.*;

public class KeelJDBCForMySQL {

    private final String jdbcConnectionString;
    private final String username;
    private final String password;

    public KeelJDBCForMySQL(KeelMySQLOptions options) {
        this.jdbcConnectionString = options.buildJDBCConnectionString();
        this.username = options.getUsername();
        this.password = options.getPassword();
    }

    private final ThreadLocalStatementWrapper threadLocalStatementWrapper = new ThreadLocalStatementWrapper(this);

    /**
     * @param sql
     * @param statement
     * @return
     * @throws SQLException
     * @since 1.9 became static
     */
    public static ResultMatrix queryForSelection(String sql, Statement statement) throws SQLException {
        ResultSet resultSet = statement.executeQuery(sql);
        ResultMatrixWithJDBC resultMatrixWithJDBC = new ResultMatrixWithJDBC(resultSet);
        resultSet.close();
        return resultMatrixWithJDBC;
    }

    /**
     * @param sql
     * @param statement
     * @return
     * @throws SQLException
     * @since 1.9 became static
     */
    public static ResultMatrix executeForInsertion(String sql, Statement statement) throws SQLException {
        int afx = statement.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS);

        long autoIncKeyFromApi = -1;

        ResultSet rs = statement.getGeneratedKeys();

        if (rs.next()) {
            autoIncKeyFromApi = rs.getLong(1);
        }

        rs.close();

        ResultMatrixWithJDBC resultMatrixWithJDBC = new ResultMatrixWithJDBC();
        resultMatrixWithJDBC.setAffectedRows(afx);
        resultMatrixWithJDBC.setLastInsertedID(autoIncKeyFromApi);
        return resultMatrixWithJDBC;
    }

    /**
     * @param sql       SQL
     * @param statement Statement
     * @return
     * @throws SQLException
     * @since 1.9 became static
     */
    public static ResultMatrix executeForModification(String sql, Statement statement) throws SQLException {
        int afx = statement.executeUpdate(sql);
        ResultMatrixWithJDBC resultMatrixWithJDBC = new ResultMatrixWithJDBC();
        resultMatrixWithJDBC.setAffectedRows(afx);
        return resultMatrixWithJDBC;
    }

    public Statement createStatement(boolean autoCommit) throws SQLException {
        Connection connection = DriverManager.getConnection(jdbcConnectionString, username, password);
        connection.setAutoCommit(autoCommit);
        return connection.createStatement();
    }

    /**
     * @param statement
     * @throws SQLException
     * @since 1.9 became static
     */
    public static void closeStatement(Statement statement) throws SQLException {
        Connection connection = statement.getConnection();
        statement.close();
        statement.close();
    }

    public Statement begin() throws SQLException {
        Connection connection = DriverManager.getConnection(jdbcConnectionString, username, password);
        connection.setAutoCommit(false);
        return connection.createStatement();
    }

    /**
     * @param statement the statement
     * @throws SQLException
     * @since 1.9 became static
     */
    public static void commit(Statement statement) throws SQLException {
        Connection connection = statement.getConnection();
        connection.commit();
        statement.close();
        statement.close();
    }

    /**
     * @param statement the statement
     * @throws SQLException
     * @since 1.9 became static
     */
    public static void rollback(Statement statement) throws SQLException {
        Connection connection = statement.getConnection();
        connection.rollback();
        statement.close();
        statement.close();
    }

    public ThreadLocalStatementWrapper getThreadLocalStatementWrapper() {
        return threadLocalStatementWrapper;
    }
}
