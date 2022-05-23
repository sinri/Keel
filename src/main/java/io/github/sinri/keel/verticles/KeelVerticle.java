package io.github.sinri.keel.verticles;

import io.github.sinri.keel.Keel;
import io.github.sinri.keel.core.logger.KeelLogger;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.sqlclient.SqlConnection;

/**
 * 在 Keel 2.0 中，有一个异象，是将所有逻辑扔进 Verticle 里运行来模拟同一线程。
 *
 * @since 2.0
 */
abstract public class KeelVerticle extends AbstractVerticle implements VerticleAbleToUndeployItself {

    protected KeelLogger getLogger() {
        return Keel.getKeelLoggerInContext(context);
    }

    protected final void setLogger(KeelLogger logger) {
        Keel.setKeelLoggerInContext(this.context, logger);
    }

    public JsonObject getVerticleInfo() {
        return new JsonObject()
                .put("class", this.getClass().getName())
                .put("config", this.config());
    }

    protected final SqlConnection getMySqlConnection() {
        return Keel.getMySqlConnectionInContext(this.context);
    }

    protected final void setMySqlConnection(SqlConnection sqlConnect) {
        Keel.setMySqlConnectionInContext(this.context, sqlConnect);
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
