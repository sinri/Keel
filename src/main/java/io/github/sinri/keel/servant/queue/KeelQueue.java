package io.github.sinri.keel.servant.queue;

import io.github.sinri.keel.facade.Keel;
import io.github.sinri.keel.facade.async.KeelAsyncKit;
import io.github.sinri.keel.verticles.KeelVerticleBase;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;

import javax.annotation.Nonnull;

/**
 * 标准的队列服务实现。
 * <p>
 * 仅用于单节点模式。
 *
 * @since 2.1
 */
public abstract class KeelQueue extends KeelVerticleBase {
    private KeelQueueNextTaskSeeker nextTaskSeeker;
    private QueueWorkerPoolManager queueWorkerPoolManager;
    private SignalReader signalReader;
    private QueueStatus queueStatus = QueueStatus.INIT;

    public QueueStatus getQueueStatus() {
        return queueStatus;
    }

    protected KeelQueue setQueueStatus(QueueStatus queueStatus) {
        this.queueStatus = queueStatus;
        return this;
    }

    /**
     * Create a new instance of QueueWorkerPoolManager when routine starts.
     * By default, it uses an unlimited pool, this could be override if needed.
     *
     * @since 3.0.9
     */
    protected @Nonnull QueueWorkerPoolManager getQueueWorkerPoolManager() {
        return new QueueWorkerPoolManager(0);
    }

    /**
     * Create a new instance of KeelQueueNextTaskSeeker when routine starts.
     */
    abstract protected @Nonnull KeelQueueNextTaskSeeker getNextTaskSeeker();

    /**
     * Create a new instance of SignalReader when routine starts.
     *
     * @since 3.0.1
     */
    abstract protected @Nonnull SignalReader getSignalReader();

    public void start() {
        this.queueStatus = QueueStatus.RUNNING;

        try {
            routine();
        } catch (Exception e) {
            getLogger().exception(e, "Exception in routine");
            undeployMe();
        }
    }

    protected final void routine() {
        getLogger().debug("KeelQueue::routine start");
        this.signalReader = getSignalReader();
        this.queueWorkerPoolManager = getQueueWorkerPoolManager();
        this.nextTaskSeeker = getNextTaskSeeker();

        Future.succeededFuture()
                .compose(v -> {
                    return signalReader.readSignal();
                })
                .recover(throwable -> {
                    getLogger().debug("AS IS. Failed to read signal: " + throwable.getMessage());
                    if (getQueueStatus() == QueueStatus.STOPPED) {
                        return Future.succeededFuture(QueueSignal.STOP);
                    } else {
                        return Future.succeededFuture(QueueSignal.RUN);
                    }
                })
                .compose(signal -> {
                    if (signal == QueueSignal.STOP) {
                        return this.whenSignalStopCame();
                    } else if (signal == QueueSignal.RUN) {
                        return this.whenSignalRunCame(nextTaskSeeker);
                    } else {
                        return Future.failedFuture("Unknown Signal");
                    }
                })
                .eventually(v -> {
                    long waitingMs = nextTaskSeeker.waitingMs();
                    getLogger().debug("set timer for next routine after " + waitingMs + " ms");
                    Keel.getVertx().setTimer(waitingMs, timerID -> routine());
                    return Future.succeededFuture();
                })
        ;
    }

    private Future<Void> whenSignalStopCame() {
        if (getQueueStatus() == QueueStatus.RUNNING) {
            this.queueStatus = QueueStatus.STOPPED;
            getLogger().notice("Signal Stop Received");
        }
        return Future.succeededFuture();
    }

    private Future<Void> whenSignalRunCame(KeelQueueNextTaskSeeker nextTaskSeeker) {
        this.queueStatus = QueueStatus.RUNNING;

        return KeelAsyncKit.repeatedlyCall(routineResult -> {
                    if (this.queueWorkerPoolManager.isBusy()) {
                        return KeelAsyncKit.sleep(1_000L);
                    }

                    return Future.succeededFuture()
                            .compose(v -> nextTaskSeeker.get())
                            .compose(task -> {
                                if (task == null) {
                                    // 队列里已经空了，不必再找
                                    getLogger().debug("No more task todo");
                                    // 通知 FutureUntil 结束
                                    routineResult.stop();
                                    return Future.succeededFuture();
                                }

                                // 队列里找出来一个task, deploy it (至于能不能跑起来有没有锁就不管了)
                                getLogger().info("To run task: " + task.getTaskReference());
                                getLogger().info("Trusted that task is already locked by seeker: " + task.getTaskReference());

                                // since 3.0.9
                                task.setQueueWorkerPoolManager(this.queueWorkerPoolManager);

                                return Future.succeededFuture()
                                        .compose(v -> task.deployMe(new DeploymentOptions().setWorker(true)))
                                        .compose(
                                                deploymentID -> {
                                                    getLogger().info("TASK [" + task.getTaskReference() + "] VERTICLE DEPLOYED: " + deploymentID);
                                                    // 通知 FutureUntil 继续下一轮
                                                    return Future.succeededFuture();
                                                },
                                                throwable -> {
                                                    getLogger().exception(throwable, "CANNOT DEPLOY TASK [" + task.getTaskReference() + "] VERTICLE");
                                                    // 通知 FutureUntil 继续下一轮
                                                    return Future.succeededFuture();
                                                }
                                        );

                            });
                })
                .recover(throwable -> {
                    getLogger().exception(throwable, "KeelQueue 递归找活干里出现了奇怪的故障");
                    return Future.succeededFuture();
                });
    }

    @Override
    public void stop() {
        this.queueStatus = QueueStatus.STOPPED;
    }

    public enum QueueSignal {
        RUN,
        STOP
    }

    public enum QueueStatus {
        INIT,
        RUNNING,
        STOPPED
    }

    public interface SignalReader {
        Future<KeelQueue.QueueSignal> readSignal();
    }
}
