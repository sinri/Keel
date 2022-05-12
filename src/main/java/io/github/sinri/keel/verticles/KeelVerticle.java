package io.github.sinri.keel.verticles;

import io.github.sinri.keel.Keel;
import io.github.sinri.keel.core.logger.KeelLogger;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.sqlclient.SqlConnection;

import java.sql.Statement;

/**
 * 在 Keel 2.0 中，有一个异象，是将所有逻辑扔进 Verticle 里运行来模拟同一线程。
 *
 * @since 2.0
 */
abstract public class KeelVerticle extends AbstractVerticle implements VerticleAbleToUndeployItself {

    protected KeelLogger getLogger() {
        return Keel.getKeelLoggerInContext();
    }

    protected final void setLogger(KeelLogger logger) {
        Keel.setKeelLoggerInContext(logger);
    }

    protected final SqlConnection getMySqlConnection() {
        return Keel.getMySqlConnectionInContext();
    }

    protected final void setMySqlConnection(SqlConnection sqlConnect) {
        Keel.setMySqlConnectionInContext(sqlConnect);
    }

    protected final Statement getJDBCStatement() {
        return Keel.getJDBCStatementInContext();
    }

    protected final void setJDBCStatement(Statement statement) {
        Keel.setJDBCStatementInContext(statement);
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
