package io.github.sinri.keel.servant.queue;

import io.github.sinri.keel.verticles.KeelVerticleImplWithIssueRecorder;
import io.vertx.core.Future;
import org.jetbrains.annotations.NotNull;


/**
 * @since 2.1
 */
public abstract class KeelQueueTask extends KeelVerticleImplWithIssueRecorder<QueueTaskIssueRecord> {
    QueueWorkerPoolManager queueWorkerPoolManager;

    final void setQueueWorkerPoolManager(QueueWorkerPoolManager queueWorkerPoolManager) {
        this.queueWorkerPoolManager = queueWorkerPoolManager;
    }

    @NotNull
    abstract public String getTaskReference();

    @NotNull
    abstract public String getTaskCategory();

    @Override
    protected final void startAsKeelVerticle() {
        this.queueWorkerPoolManager.whenOneWorkerStarts();

        Future.succeededFuture()
                .compose(v -> {
                    notifyAfterDeployed();
                    return Future.succeededFuture();
                })
                .compose(v -> run())
                .recover(throwable -> {
                    getIssueRecorder().exception(throwable, r -> r.message("KeelQueueTask Caught throwable from Method run"));
                    return Future.succeededFuture();
                })
                .eventually(() -> {
                    getIssueRecorder().info(r -> r.message("KeelQueueTask to undeploy"));
                    notifyBeforeUndeploy();
                    return undeployMe().onSuccess(done -> {
                        this.queueWorkerPoolManager.whenOneWorkerEnds();
                    });
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
