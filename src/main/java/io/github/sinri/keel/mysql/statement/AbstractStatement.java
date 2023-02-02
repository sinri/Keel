package io.github.sinri.keel.mysql.statement;

import io.github.sinri.keel.logger.event.KeelEventLogger;
import io.github.sinri.keel.mysql.matrix.ResultMatrix;
import io.vertx.core.Future;
import io.vertx.sqlclient.SqlConnection;

import java.util.UUID;

/**
 * @since 1.7
 */
abstract public class AbstractStatement {
    protected static KeelEventLogger sqlAuditLogger = KeelEventLogger.silentLogger();
    protected static String SQL_COMPONENT_SEPARATOR = " ";//"\n";
    protected final String statement_uuid;
    private String remarkAsComment = "";

    public AbstractStatement() {
        this.statement_uuid = UUID.randomUUID().toString();
    }

    public static KeelEventLogger getSqlAuditLogger() {
        return sqlAuditLogger;
    }

    public static void setSqlAuditLogger(KeelEventLogger sqlAuditLogger) {
        AbstractStatement.sqlAuditLogger = sqlAuditLogger;
    }

    public static void setSqlComponentSeparator(String sqlComponentSeparator) {
        SQL_COMPONENT_SEPARATOR = sqlComponentSeparator;
    }

    /**
     * @return The SQL Generated
     */
    public abstract String toString();

    protected String getRemarkAsComment() {
        return remarkAsComment;
    }

    public AbstractStatement setRemarkAsComment(String remarkAsComment) {
        if (remarkAsComment == null) {
            remarkAsComment = "";
        }
        remarkAsComment = remarkAsComment.replaceAll("[\\r\\n]+", "¦");
        this.remarkAsComment = remarkAsComment;
        return this;
    }

    /**
     * @param sql
     * @return
     * @since 3.0.0
     */
    public static AbstractStatement buildWithRawSQL(String sql) {
        return new AbstractStatement() {
            @Override
            public String toString() {
                return sql;
            }
        };
    }

    /**
     * 在给定的SqlConnection上执行SQL，异步返回ResultMatrix，或异步报错。
     * （如果SQL审计日志记录器可用）将为审计记录执行的SQL和执行结果，以及任何异常。
     *
     * @param sqlConnection Fetched from Pool
     * @return the result matrix wrapped in a future, any error would cause a failed future
     * @since 2.8 将整个运作体加入了try-catch，统一加入审计日志，出现异常时一律异步报错。
     * @since 3.0.0 removed try-catch
     */
    public final Future<ResultMatrix> execute(SqlConnection sqlConnection) {
        return Future.succeededFuture(this.toString())
                .compose(sql -> {
                    getSqlAuditLogger().info(statement_uuid + " sql: " + sql);
                    return sqlConnection.preparedQuery(sql).execute()
                            .compose(rows -> {
                                ResultMatrix resultMatrix = ResultMatrix.create(rows);
                                return Future.succeededFuture(resultMatrix);
                            });
                })
                .compose(resultMatrix -> {
                    getSqlAuditLogger().info(
                            event -> event.message(statement_uuid + " done")
                                    .put("TotalAffectedRows", resultMatrix.getTotalAffectedRows())
                                    .put("TotalFetchedRows", resultMatrix.getTotalFetchedRows())
                    );
                    return Future.succeededFuture(resultMatrix);
                }, throwable -> {
                    getSqlAuditLogger().exception(throwable, statement_uuid + " execute failed");
                    return Future.failedFuture(throwable);
                });
    }
}
