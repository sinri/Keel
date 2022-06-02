package io.github.sinri.keel.verticles;

import io.github.sinri.keel.Keel;
import io.github.sinri.keel.core.logger2.KeelLogger;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.sqlclient.SqlConnection;

import java.util.function.Function;

/**
 * 在 Keel 2.0 中，有一个异象，是将所有逻辑扔进 Verticle 里运行来模拟同一线程。
 *
 * @since 2.0
 */
abstract public class KeelVerticle extends AbstractVerticle implements VerticleAbleToUndeployItself {

    private KeelLogger logger = KeelLogger.silentLogger();

    /**
     * @since 2.4 do not rely on context anymore
     */
    protected KeelLogger getLogger() {
        return logger;
    }

    /**
     * @since 2.4 do not rely on context anymore
     */
    protected final void setLogger(KeelLogger logger) {
        this.logger = logger;
    }

    public JsonObject getVerticleInfo() {
        return new JsonObject()
                .put("class", this.getClass().getName())
                .put("config", this.config())
                .put("deployment_id", this.deploymentID());
    }

    /**
     * @since 2.4
     */
    protected final <R> Future<R> executeWithMySQL(Function<SqlConnection, Future<R>> executor) {
        return Keel.getMySQLKit().getPool().withConnection(executor);
    }

    /**
     * @since 2.4
     */
    protected final <R> Future<R> executeWithMySQL(String dataSourceName, Function<SqlConnection, Future<R>> executor) {
        return Keel.getMySQLKit(dataSourceName).getPool().withConnection(executor);
    }

    /**
     * @since 2.4
     */
    protected final <R> Future<R> executeWithinMySQLTransaction(Function<SqlConnection, Future<R>> executor) {
        return Keel.getMySQLKit().getPool().withTransaction(executor);
    }

    /**
     * @since 2.4
     */
    protected final <R> Future<R> executeWithinMySQLTransaction(String dataSourceName, Function<SqlConnection, Future<R>> executor) {
        return Keel.getMySQLKit(dataSourceName).getPool().withTransaction(executor);
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
