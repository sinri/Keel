package io.github.sinri.keel.servant.queue;

import io.github.sinri.keel.logger.issue.recorder.KeelIssueRecorder;
import io.github.sinri.keel.verticles.KeelVerticleBase;
import io.vertx.core.Future;

import javax.annotation.Nonnull;

/**
 * @since 2.1
 */
public abstract class KeelQueueTask extends KeelVerticleBase<QueueTaskIssueRecord> {
    QueueWorkerPoolManager queueWorkerPoolManager;

    final void setQueueWorkerPoolManager(QueueWorkerPoolManager queueWorkerPoolManager) {
        this.queueWorkerPoolManager = queueWorkerPoolManager;
    }

    @Nonnull
    abstract public String getTaskReference();

    @Nonnull
    abstract public String getTaskCategory();

    /**
     * @since 3.2.0
     */
    abstract protected KeelIssueRecorder<QueueTaskIssueRecord> prepareRoutineIssueRecord();

    // as verticle
    public final void start() {
        KeelIssueRecorder<QueueTaskIssueRecord> issueRecorder = prepareRoutineIssueRecord();
        setIssueRecorder(issueRecorder);

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
