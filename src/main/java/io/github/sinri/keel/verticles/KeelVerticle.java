package io.github.sinri.keel.verticles;

import io.github.sinri.keel.Keel;
import io.github.sinri.keel.core.logger.KeelLogger;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.sqlclient.SqlConnection;

import java.util.function.Function;

/**
 * 在 Keel 2.0 中，有一个异象，是将所有逻辑扔进 Verticle 里运行来模拟同一线程。
 *
 * @since 2.0
 */
abstract public class KeelVerticle extends AbstractVerticle implements KeelVerticleInterface {

    private KeelLogger logger = KeelLogger.silentLogger();

    /**
     * @since 2.4 do not rely on context anymore
     * @since 2.8 become public
     */
    public KeelLogger getLogger() {
        return logger;
    }

    /**
     * @since 2.4 do not rely on context anymore
     * @since 2.7 became public
     */
    public final void setLogger(KeelLogger logger) {
        this.logger = logger;
    }

    /**
     * @since 2.4
     */
    @Deprecated(since = "2.8", forRemoval = true)
    protected final <R> Future<R> executeWithMySQL(Function<SqlConnection, Future<R>> executor) {
        return Keel.getMySQLKit().withConnection(executor);
    }

    /**
     * @since 2.4
     */
    @Deprecated(since = "2.8", forRemoval = true)
    protected final <R> Future<R> executeWithMySQL(String dataSourceName, Function<SqlConnection, Future<R>> executor) {
        return Keel.getMySQLKit(dataSourceName).withConnection(executor);
    }

    /**
     * @since 2.4
     */
    @Deprecated(since = "2.8", forRemoval = true)
    protected final <R> Future<R> executeWithinMySQLTransaction(Function<SqlConnection, Future<R>> executor) {
        return Keel.getMySQLKit().withTransaction(executor);
    }

    /**
     * @since 2.4
     */
    @Deprecated(since = "2.8", forRemoval = true)
    protected final <R> Future<R> executeWithinMySQLTransaction(String dataSourceName, Function<SqlConnection, Future<R>> executor) {
        return Keel.getMySQLKit(dataSourceName).withTransaction(executor);
    }

    @Deprecated(since = "2.8")
    public final Future<String> deployMeAsWorker() {
        return Keel.getVertx().deployVerticle(this, new DeploymentOptions().setWorker(true));
    }

    @Deprecated(since = "2.8")
    public final Future<String> deployMeAsWorker(DeploymentOptions deploymentOptions) {
        return Keel.getVertx().deployVerticle(this, deploymentOptions.setWorker(true));
    }
}
