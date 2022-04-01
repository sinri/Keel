package io.github.sinri.keel.verticles.sync;

import io.github.sinri.keel.Keel;
import io.github.sinri.keel.core.logger.KeelLogger;
import io.github.sinri.keel.verticles.VerticleAbleToUndeployItself;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;

/**
 * @since 1.14
 * 利用 vertx.deployVerticle(KeelWorkerVerticle.class, new DeploymentOptions().setWorker(true)) 这一特性，
 * 让 vertx 使用特定的线程 KeelWorkerVerticle 中的同步代码
 */
@Deprecated
abstract public class KeelSyncWorkerVerticle<R> extends AbstractVerticle implements VerticleAbleToUndeployItself {
    private final Report<R> report = new Report<>();
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
    protected void prepare() {
        logger = Keel.standaloneLogger(getAspectForLogging()).setCategoryPrefix(this.deploymentID());
        logger.info("prepare with config read from context", config());
    }

    /**
     * 实现要完成的具体工作。
     * 要求此处使用同步作业，不要使用任何异步代码。
     * 如果有返回值，则使用适当的实现，使返回值可通过 getResult 方法获取。
     */
    abstract protected R syncExecute() throws Throwable;

    @Override
    public void start() throws Exception {
        super.start();
        prepare();
        try {
            var r = syncExecute();
            getReport().claimDone(r);
        } catch (Throwable e) {
            getReport().claimFailed(e);
        }
        this.logger.notice("syncExecute executed");
    }

    /**
     * 报告执行状况
     *
     * @return 当未执行完时为返回 null。
     */
    public Report<R> getReport() {
        return report;
    }

    public Future<R> deployAndRun() {
        return deployAndRun(null);
    }

    public Future<R> deployAndRun(JsonObject config) {
        var deploymentOptions = new DeploymentOptions().setWorker(true);
        if (config != null) deploymentOptions.setConfig(config);
        return Keel.getVertx()
                .deployVerticle(this, deploymentOptions)
                .compose(deploymentID -> {
                    getLogger().info("Verticle Deployment Finished, ready to undeploy it ...");
                    var report = this.getReport();
                    return Keel.getVertx()
                            .undeploy(deploymentID)
                            .compose(v -> {
                                if (report.isSuccessfullyDone() == null) {
                                    getLogger().error("Verticle died before starting!");
                                    return Future.failedFuture("Verticle died before starting");
                                } else if (!report.isSuccessfullyDone()) {
                                    getLogger().exception("Verticle Failed", report.getFailure());
                                    return Future.failedFuture(report.getFailure());
                                }
                                getLogger().info("Verticle reported result: " + report.getResult().toString());
                                return Future.succeededFuture(report.getResult());
                            });
                });
    }

    @Override
    public Future<Void> undeployMe() {
        return VerticleAbleToUndeployItself.undeploy(this.deploymentID());
    }

    protected static class Report<T> {
        private Boolean done = null;
        private Throwable throwable = null;
        private T result = null;

        /**
         * 是否按预期执行完毕。
         *
         * @return 未开始或者异常未捕捉为null；执行过程出现异常而未按预期中止则为false；成功为true。
         */
        public Boolean isSuccessfullyDone() {
            return done;
        }

        /**
         * 报告执行过程未按预期中止的状况下所出现异常。
         *
         * @return Throwable
         */
        public Throwable getFailure() {
            return throwable;
        }

        public T getResult() {
            return result;
        }

        public void claimDone(T result) {
            this.done = true;
            this.result = result;
        }

        public void claimFailed(Throwable throwable) {
            this.done = false;
            this.throwable = throwable;
        }
    }

}
