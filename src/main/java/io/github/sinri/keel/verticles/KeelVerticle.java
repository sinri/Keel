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

    @Deprecated(since = "2.4", forRemoval = true)
    protected KeelLogger getLogger() {
        if (context == null) {
            return Keel.outputLogger(getClass().getName());
        }
        return Keel.getKeelLoggerInContext(context);
    }

    @Deprecated(since = "2.4", forRemoval = true)
    protected final void setLogger(KeelLogger logger) {
        if (this.context != null) {
            Keel.setKeelLoggerInContext(this.context, logger);
        }
    }

    public JsonObject getVerticleInfo() {
        return new JsonObject()
                .put("class", this.getClass().getName())
                .put("config", this.config());
    }

    @Deprecated(since = "2.4", forRemoval = true)
    protected final SqlConnection getMySqlConnection() {
        return Keel.getMySqlConnectionInContext(this.context);
    }

    @Deprecated(since = "2.4", forRemoval = true)
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
