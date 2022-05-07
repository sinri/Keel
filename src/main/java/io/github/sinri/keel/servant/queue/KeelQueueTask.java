package io.github.sinri.keel.servant.queue;

import io.github.sinri.keel.core.logger.KeelLogger;
import io.github.sinri.keel.verticles.KeelVerticle;
import io.vertx.core.Future;

/**
 * @since 2.1
 */
public abstract class KeelQueueTask extends KeelVerticle {
    public KeelQueueTask() {
        super();
    }

    abstract public String getTaskReference();

    abstract public String getTaskCategory();

    abstract public boolean isRunSerially();

    abstract protected KeelLogger prepareLogger();

    /**
     * 被设计在seeker.seek方法中调用
     *
     * @return
     */
    public Future<Void> lockTaskBeforeDeployment() {
        // 如果需要就重载此方法
        return Future.succeededFuture();
    }

    // as verticle
    public final void start() {
        setLogger(prepareLogger());
        notifyAfterDeployed();
        run()
                .recover(throwable -> {
                    getLogger().exception("KeelQueueTask Caught throwable from Method run", throwable);
                    return Future.succeededFuture();
                })
                .eventually(v -> {
                    getLogger().info("KeelQueueTask to undeploy");
                    notifyBeforeUndeploy();
                    return undeployMe();
                });
    }

    abstract protected Future<Void> run();

    protected void notifyAfterDeployed() {
        // do nothing by default
    }

    protected void notifyBeforeUndeploy() {
        // do nothing by default
    }
}
