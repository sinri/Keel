package io.github.sinri.keel.mysql.statement;

import io.github.sinri.keel.mysql.DuplexExecutorForMySQL;
import io.github.sinri.keel.mysql.exception.KeelSQLResultRowIndexError;
import io.github.sinri.keel.mysql.jdbc.KeelJDBCForMySQL;
import io.github.sinri.keel.mysql.matrix.AbstractRow;
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
    @Deprecated
    public ResultMatrix blockedExecute(Statement statement) throws SQLException {
        return KeelJDBCForMySQL.queryForSelection(this.toString(), statement);
    }

    @Deprecated
    public <T extends AbstractRow> DuplexExecutorForMySQL<T> getOneTableRowFetcher(Class<T> classOfTableRow) {
        return AbstractRow.buildTableRowFetcher(this, classOfTableRow);
    }

    @Deprecated
    public <T extends AbstractRow> DuplexExecutorForMySQL<List<T>> getTableRowListFetcher(Class<T> classOfTableRow) {
        return AbstractRow.buildTableRowListFetcher(this, classOfTableRow);
    }

    @Deprecated
    public DuplexExecutorForMySQL<List<Long>> getLongColumnFetcher(String fieldName) {
        return new DuplexExecutorForMySQL<>(
                sqlConnection -> this.execute(sqlConnection)
                        .compose(resultMatrix -> Future.succeededFuture(resultMatrix.getOneColumnAsLong(fieldName))),
                statement -> this.blockedExecute(statement).getOneColumnAsLong(fieldName)
        );
    }

    @Deprecated
    public DuplexExecutorForMySQL<List<Integer>> getIntegerColumnFetcher(String fieldName) {
        return new DuplexExecutorForMySQL<>(
                sqlConnection -> this.execute(sqlConnection)
                        .compose(resultMatrix -> Future.succeededFuture(resultMatrix.getOneColumnAsInteger(fieldName))),
                statement -> this.blockedExecute(statement).getOneColumnAsInteger(fieldName)
        );
    }

    @Deprecated
    public DuplexExecutorForMySQL<List<Numeric>> getNumberColumnFetcher(String fieldName) {
        return new DuplexExecutorForMySQL<>(
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
    @Deprecated
    public DuplexExecutorForMySQL<List<String>> getStringColumnFetcher(String fieldName) {
        return new DuplexExecutorForMySQL<>(
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
    @Deprecated
    public DuplexExecutorForMySQL<String> getStringCellFetcher(String fieldName) {
        return new DuplexExecutorForMySQL<>(
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
    @Deprecated
    public DuplexExecutorForMySQL<Numeric> getNumericCellFetcher(String fieldName) {
        return new DuplexExecutorForMySQL<>(
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
