package io.github.sinri.keel.servant.queue;

import io.github.sinri.keel.facade.Keel;
import io.github.sinri.keel.logger.event.KeelEventLogger;
import io.github.sinri.keel.verticles.KeelVerticleBase;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;

/**
 * 标准的队列服务实现。
 * <p>
 * 仅用于单节点模式。
 *
 * @since 2.1
 */
public abstract class KeelQueue extends KeelVerticleBase {

    private final Keel keel;
    private QueueStatus queueStatus = QueueStatus.INIT;

    public KeelQueue(Keel keel) {
        super();
        this.keel = keel;

        setLogger(prepareLogger());
    }

    public QueueStatus getQueueStatus() {
        return queueStatus;
    }

    protected KeelQueue setQueueStatus(QueueStatus queueStatus) {
        this.queueStatus = queueStatus;
        return this;
    }

    abstract protected KeelEventLogger prepareLogger();


    abstract protected KeelQueueNextTaskSeeker getNextTaskSeeker();

    public void start() {
        // 部署之后重新加载一遍
        setLogger(prepareLogger());

        this.queueStatus = QueueStatus.RUNNING;

        try {
            routine();
        } catch (Exception e) {
            getLogger().exception(e, "Exception in routine");
            undeployMe();
        }
    }

    abstract protected Future<QueueSignal> readSignal();

    protected final void routine() {
        getLogger().debug("KeelQueue::routine start");
        KeelQueueNextTaskSeeker nextTaskSeeker = getNextTaskSeeker();

        Future.succeededFuture()
                .compose(v -> {
                    return readSignal();
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
                    keel.setTimer(waitingMs, timerID -> routine());
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

        return keel.repeatedlyCall(routineResult -> {
                    return Future.succeededFuture()
                            .compose(v -> nextTaskSeeker.get())
                            .compose(task -> {
                                if (task == null) {
                                    // 队列里已经空了，不必再找
                                    getLogger().debug("No more task todo");
                                    // 通知 FutureUntil 结束
                                    routineResult.stop();
                                    return Future.succeededFuture();
                                } else {
                                    // 队列里找出来一个task, deploy it (至于能不能跑起来有没有锁就不管了)
                                    getLogger().info("To run task: " + task.getTaskReference());
                                    getLogger().info("Trusted that task is already locked by seeker: " + task.getTaskReference());
                                    return Future.succeededFuture()
                                            .compose(v -> task.deployMe(keel, new DeploymentOptions().setWorker(true)))
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
                                }
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

//        Keel.unregisterDeployedKeelVerticle(this.deploymentID());
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
}
