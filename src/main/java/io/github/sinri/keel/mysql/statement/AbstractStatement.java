package io.github.sinri.keel.mysql.statement;

import io.github.sinri.keel.core.logger.KeelLogger;
import io.github.sinri.keel.mysql.matrix.ResultMatrix;
import io.github.sinri.keel.mysql.matrix.ResultMatrixWithVertx;
import io.vertx.core.Future;
import io.vertx.sqlclient.SqlConnection;

/**
 * @since 1.7
 */
abstract public class AbstractStatement {
    protected static KeelLogger sqlAuditLogger = KeelLogger.buildSilentLogger();
    private String remarkAsComment = "";

    public static KeelLogger getSqlAuditLogger() {
        return sqlAuditLogger;
    }

    /**
     * @return The SQL Generated
     */
    public abstract String toString();

    public static void setSqlAuditLogger(KeelLogger sqlAuditLogger) {
        AbstractStatement.sqlAuditLogger = sqlAuditLogger;
    }

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

    protected static String SQL_COMPONENT_SEPARATOR = " ";//"\n";

    public static void setSqlComponentSeparator(String sqlComponentSeparator) {
        SQL_COMPONENT_SEPARATOR = sqlComponentSeparator;
    }

    /**
     * @param sqlConnection Fetched from Pool
     * @return the result matrix wrapped in a future, any error would cause a failed future
     */
    public final Future<ResultMatrix> execute(SqlConnection sqlConnection) {
        String sql = this.toString();
        getSqlAuditLogger().info(sql);
        return sqlConnection.preparedQuery(sql)
                .execute()
                .compose(rows -> Future.succeededFuture(new ResultMatrixWithVertx(rows)));
    }
}
