package io.github.sinri.keel.mysql.statement;

import io.github.sinri.keel.mysql.MySQLExecutor;
import io.github.sinri.keel.mysql.exception.KeelSQLResultRowIndexError;
import io.github.sinri.keel.mysql.jdbc.KeelJDBCForMySQL;
import io.github.sinri.keel.mysql.matrix.AbstractTableRow;
import io.github.sinri.keel.mysql.matrix.ResultMatrix;
import io.vertx.core.Future;
import io.vertx.sqlclient.data.Numeric;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;


/**
 * @since 1.10
 */
public abstract class AbstractReadStatement extends AbstractStatement {
    @Override
    public ResultMatrix blockedExecute(Statement statement) throws SQLException {
        return KeelJDBCForMySQL.queryForSelection(this.toString(), statement);
    }

    public <T extends AbstractTableRow> MySQLExecutor<T> getOneTableRowFetcher(Class<T> classOfTableRow) {
        return AbstractTableRow.buildTableRowFetcher(this, classOfTableRow);
    }

    public <T extends AbstractTableRow> MySQLExecutor<List<T>> getTableRowListFetcher(Class<T> classOfTableRow) {
        return AbstractTableRow.buildTableRowListFetcher(this, classOfTableRow);
    }

    /**
     * @param fieldName
     * @return
     * @since 1.13
     */
    public MySQLExecutor<List<Long>> getLongColumnFetcher(String fieldName) {
        return MySQLExecutor.build(
                sqlConnection -> this.execute(sqlConnection)
                        .compose(resultMatrix -> Future.succeededFuture(resultMatrix.getOneColumnAsLong(fieldName))),
                statement -> this.blockedExecute(statement).getOneColumnAsLong(fieldName)
        );
    }

    /**
     * @param fieldName
     * @return
     * @since 1.13
     */
    public MySQLExecutor<List<Integer>> getIntegerColumnFetcher(String fieldName) {
        return MySQLExecutor.build(
                sqlConnection -> this.execute(sqlConnection)
                        .compose(resultMatrix -> Future.succeededFuture(resultMatrix.getOneColumnAsInteger(fieldName))),
                statement -> this.blockedExecute(statement).getOneColumnAsInteger(fieldName)
        );
    }

    /**
     * @param fieldName
     * @return
     * @since 1.13
     */
    public MySQLExecutor<List<Numeric>> getNumberColumnFetcher(String fieldName) {
        return MySQLExecutor.build(
                sqlConnection -> this.execute(sqlConnection)
                        .compose(resultMatrix -> Future.succeededFuture(resultMatrix.getOneColumnAsNumeric(fieldName))),
                statement -> this.blockedExecute(statement).getOneColumnAsNumeric(fieldName)
        );
    }

    /**
     * @param fieldName
     * @return
     * @since 1.13
     */
    public MySQLExecutor<List<String>> getStringColumnFetcher(String fieldName) {
        return MySQLExecutor.build(
                sqlConnection -> this.execute(sqlConnection)
                        .compose(resultMatrix -> Future.succeededFuture(resultMatrix.getOneColumnAsString(fieldName))),
                statement -> this.blockedExecute(statement).getOneColumnAsString(fieldName)
        );
    }

    /**
     * @param fieldName
     * @return
     * @since 1.13
     */
    public MySQLExecutor<String> getStringCellFetcher(String fieldName) {
        return MySQLExecutor.build(
                sqlConnection -> this.execute(sqlConnection)
                        .compose(resultMatrix -> {
                            try {
                                return Future.succeededFuture(resultMatrix.getOneColumnOfFirstRowAsString(fieldName));
                            } catch (KeelSQLResultRowIndexError e) {
                                return null;
                            }
                        }),
                statement -> {
                    try {
                        return this.blockedExecute(statement).getOneColumnOfFirstRowAsString(fieldName);
                    } catch (KeelSQLResultRowIndexError e) {
                        return null;
                    }
                }
        );
    }

    /**
     * @param fieldName
     * @return
     * @since 1.13
     */
    public MySQLExecutor<Numeric> getNumericCellFetcher(String fieldName) {
        return MySQLExecutor.build(
                sqlConnection -> this.execute(sqlConnection)
                        .compose(resultMatrix -> {
                            try {
                                return Future.succeededFuture(resultMatrix.getOneColumnOfFirstRowAsNumeric(fieldName));
                            } catch (KeelSQLResultRowIndexError e) {
                                return null;
                            }
                        }),
                statement -> {
                    try {
                        return this.blockedExecute(statement).getOneColumnOfFirstRowAsNumeric(fieldName);
                    } catch (KeelSQLResultRowIndexError e) {
                        return null;
                    }
                }
        );
    }
}
