package io.github.sinri.keel.mysql.statement;

import io.github.sinri.keel.logger.issue.center.KeelIssueRecordCenterAsSilent;
import io.github.sinri.keel.logger.issue.record.BaseIssueRecord;
import io.github.sinri.keel.logger.issue.recorder.KeelIssueRecorder;
import io.github.sinri.keel.mysql.matrix.ResultMatrix;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.sqlclient.SqlConnection;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @since 1.7
 */
abstract public class AbstractStatement implements AnyStatement {
    /**
     * @since 3.2.0 replace original SQL Audit Logger
     */
    protected static @NotNull KeelIssueRecorder<MySQLAuditIssueRecord> sqlAuditIssueRecorder = KeelIssueRecordCenterAsSilent.getInstance()
            .generateIssueRecorder(MySQLAuditIssueRecord.AttributeMysqlAudit, MySQLAuditIssueRecord::new);
    protected static @NotNull String SQL_COMPONENT_SEPARATOR = " ";//"\n";
    protected final @NotNull String statement_uuid;
    private @NotNull String remarkAsComment = "";

    public AbstractStatement() {
        this.statement_uuid = UUID.randomUUID().toString();
    }

    @NotNull
    public static KeelIssueRecorder<MySQLAuditIssueRecord> getSqlAuditIssueRecorder() {
        return sqlAuditIssueRecorder;
    }

    public static void setSqlAuditIssueRecorder(@NotNull KeelIssueRecorder<MySQLAuditIssueRecord> sqlAuditIssueRecorder) {
        AbstractStatement.sqlAuditIssueRecorder = sqlAuditIssueRecorder;
    }

    public static void setSqlComponentSeparator(@NotNull String sqlComponentSeparator) {
        SQL_COMPONENT_SEPARATOR = sqlComponentSeparator;
    }

    @NotNull
    protected String getRemarkAsComment() {
        return remarkAsComment;
    }

    public AbstractStatement setRemarkAsComment(@NotNull String remarkAsComment) {
        remarkAsComment = remarkAsComment.replaceAll("[\\r\\n]+", "¦");
        this.remarkAsComment = remarkAsComment;
        return this;
    }

    /**
     * @since 3.0.0
     * @since 3.0.9 Moved to AnyStatement
     */
    @Deprecated(since = "3.0.9")
    public static AbstractStatement buildWithRawSQL(String sql) {
        return AnyStatement.raw(sql);
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
    @Override
    public final Future<ResultMatrix> execute(@NotNull SqlConnection sqlConnection) {
        AtomicReference<String> theSql = new AtomicReference<>();
        return Future.succeededFuture(this.toString())
                .compose(sql -> {
                    theSql.set(sql);
                    getSqlAuditIssueRecorder().info(r -> r.setPreparation(statement_uuid, sql));
                    return sqlConnection.preparedQuery(sql).execute()
                            .compose(rows -> {
                                ResultMatrix resultMatrix = ResultMatrix.create(rows);
                                return Future.succeededFuture(resultMatrix);
                            });
                })
                .compose(resultMatrix -> {
                    getSqlAuditIssueRecorder().info(r -> r.setForDone(statement_uuid, theSql.get(), resultMatrix.getTotalAffectedRows(), resultMatrix.getTotalFetchedRows()));
                    return Future.succeededFuture(resultMatrix);
                }, throwable -> {
                    getSqlAuditIssueRecorder().exception(throwable, r -> r.setForFailed(statement_uuid, theSql.get()));
                    return Future.failedFuture(throwable);
                });
    }

    public static final class MySQLAuditIssueRecord extends BaseIssueRecord<MySQLAuditIssueRecord> {
        public static final String TopicMysqlAudit = "MysqlAudit";
        public static final String AttributeMysqlAudit = "MysqlAudit";
        public static final String KeyStatementUuid = "statement_uuid";
        public static final String KeySql = "sql";
        public static final String KeyTotalAffectedRows = "TotalAffectedRows";
        public static final String KeyTotalFetchedRows = "TotalFetchedRows";


        @Override
        public @NotNull MySQLAuditIssueRecord getImplementation() {
            return this;
        }


        @Override
        public @NotNull String topic() {
            return TopicMysqlAudit;
        }

        public MySQLAuditIssueRecord setPreparation(@NotNull String statement_uuid, @NotNull String sql) {
            this.message("MySQL query prepared.")
                    .attribute(AttributeMysqlAudit, new JsonObject()
                            .put(KeyStatementUuid, statement_uuid)
                            .put(KeySql, sql)
                    );
            return this;
        }

        public MySQLAuditIssueRecord setForDone(
                @NotNull String statement_uuid,
                @NotNull String sql,
                int totalAffectedRows,
                int totalFetchedRows
        ) {
            this.message("MySQL query executed.")
                    .attribute(AttributeMysqlAudit, new JsonObject()
                            .put(KeyStatementUuid, statement_uuid)
                            .put(KeySql, sql)
                            .put(KeyTotalFetchedRows, totalFetchedRows)
                            .put(KeyTotalAffectedRows, totalAffectedRows)
                    );
            return this;
        }

        public MySQLAuditIssueRecord setForFailed(@NotNull String statement_uuid, @NotNull String sql) {
            this.message("MySQL query failed.")
                    .attribute(AttributeMysqlAudit, new JsonObject()
                            .put(KeyStatementUuid, statement_uuid)
                            .put(KeySql, sql)
                    );
            return this;
        }
    }
}
