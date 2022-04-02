package io.github.sinri.keel.verticles;

import io.github.sinri.keel.Keel;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.sqlclient.SqlConnection;

import java.sql.Statement;
import java.util.UUID;

/**
 * 在 Keel 2.0 中，有一个异象，是将所有逻辑扔进 Verticle 里运行来模拟同一线程。
 *
 * @since 2.0
 */
abstract public class KeelVerticle extends AbstractVerticle implements VerticleAbleToUndeployItself {
    private static final String KEY_MYSQL_CONNECTION_UUID = "MySQLConnectionUUID";
    private static final String KEY_MYSQL_CONNECTION = "MySQLConnection";
    private static final String KEY_JDBC_STATEMENT_UUID = "JDBCStatementUUID";
    private static final String KEY_JDBC_STATEMENT = "JDBCStatement";

    public static SqlConnection getMySqlConnection() {
        return Keel.getVertx().getOrCreateContext().get(KEY_MYSQL_CONNECTION);
    }

    public static void setMySqlConnection(SqlConnection sqlConnection) {
        Keel.getVertx().getOrCreateContext().put(KEY_MYSQL_CONNECTION, sqlConnection);
        Keel.getVertx().getOrCreateContext().put(KEY_MYSQL_CONNECTION_UUID, UUID.randomUUID().toString());
    }

    public static String getMySqlConnectionUUID() {
        return Keel.getVertx().getOrCreateContext().get(KEY_MYSQL_CONNECTION_UUID);
    }

    public static Statement getJDBCStatement() {
        return Keel.getVertx().getOrCreateContext().get(KEY_JDBC_STATEMENT);
    }

    public static void setJDBCStatement(Statement statement) {
        Keel.getVertx().getOrCreateContext().put(KEY_JDBC_STATEMENT, statement);
        Keel.getVertx().getOrCreateContext().put(KEY_JDBC_STATEMENT_UUID, UUID.randomUUID().toString());
    }

    public static String getJDBCStatementUUID() {
        return Keel.getVertx().getOrCreateContext().get(KEY_JDBC_STATEMENT_UUID);
    }

    @Override
    public void start() throws Exception {
        super.start();
    }

    @Override
    public Future<Void> undeployMe() {
        return VerticleAbleToUndeployItself.undeploy(deploymentID());
    }

    public final Future<String> deployMe() {
        return deployMe(new DeploymentOptions());
    }

    public final Future<String> deployMe(DeploymentOptions deploymentOptions) {
        return Keel.getVertx().deployVerticle(this, deploymentOptions);
    }

    public final Future<String> deployMeAsWorker() {
        return deployMeAsWorker(new DeploymentOptions());
    }

    public final Future<String> deployMeAsWorker(DeploymentOptions deploymentOptions) {
        return Keel.getVertx().deployVerticle(this, deploymentOptions.setWorker(true));
    }
}
