package io.github.sinri.keel.verticles.async;

import io.github.sinri.keel.Keel;
import io.github.sinri.keel.core.logger.KeelLogger;
import io.github.sinri.keel.mysql.KeelMySQLKit;
import io.github.sinri.keel.verticles.VerticleAbleToUndeployItself;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;

//import static io.github.sinri.keel.mysql.KeelMySQLKit.KEY_MYSQL_CONNECTION;
//import static io.github.sinri.keel.mysql.KeelMySQLKit.KEY_MYSQL_CONNECTION_UUID;
@Deprecated
abstract public class KeelWorkerVerticleWithMySQL extends AbstractVerticle implements VerticleAbleToUndeployItself {

    private KeelLogger logger = KeelLogger.buildSilentLogger();

    public KeelLogger getLogger() {
        return logger;
    }

    protected String getAspectForLogging() {
        var x = this.getClass().getName().split("\\.");
        if (x.length == 0) {
            return "KeelSyncWorkerVerticle";
        }
        return x[x.length - 1];
    }

    /**
     * 在开始静态代码之前进行准备工作：
     * 初始化日志记录器；
     * 打印传入的动态配置。
     */
    protected Future<Void> prepare() {
        logger = Keel.standaloneLogger(getAspectForLogging()).setCategoryPrefix(this.deploymentID());
        logger.info("prepare with config read from context", config());
        return Future.succeededFuture();
    }

    @Override
    public void start() throws Exception {
        super.start();
        prepare()
                .compose(prepared -> Keel.getMySQLKit().getPool().withTransaction(sqlConnection -> {
                    KeelMySQLKit.setSqlConnectionToVerticleContext(sqlConnection);
                    return runInTransaction();
                }))
                .eventually(v -> this.undeployMe());
    }

    abstract public Future<Void> runInTransaction();

    @Override
    public Future<Void> undeployMe() {
        return VerticleAbleToUndeployItself.undeploy(this.deploymentID());
    }

    public Future<String> deployAndRun() {
        return deployAndRun(null);
    }

    public Future<String> deployAndRun(JsonObject config) {
        var deploymentOptions = new DeploymentOptions().setWorker(false);// todo confirm!
        if (config != null) deploymentOptions.setConfig(config);
        return Keel.getVertx().deployVerticle(this, deploymentOptions);
    }
}
