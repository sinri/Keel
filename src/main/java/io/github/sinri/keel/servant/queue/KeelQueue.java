package io.github.sinri.keel.servant.queue;

import io.github.sinri.keel.Keel;
import io.github.sinri.keel.core.controlflow.FutureRecursion;
import io.github.sinri.keel.core.logger2.KeelLogger;
import io.github.sinri.keel.verticles.KeelVerticle;
import io.vertx.core.Future;

/**
 * @since 2.1
 */
public abstract class KeelQueue extends KeelVerticle {

    /**
     * 部署之前可以与部署之后的日志器不同实例，也可以相同
     */
    private KeelLogger logger;
    private QueueStatus queueStatus = QueueStatus.INIT;

    public KeelQueue() {
        super();

        this.logger = prepareLogger();
    }

    public QueueStatus getQueueStatus() {
        return queueStatus;
    }

    protected KeelQueue setQueueStatus(QueueStatus queueStatus) {
        this.queueStatus = queueStatus;
        return this;
    }

    abstract protected KeelLogger prepareLogger();

    @Override
    public KeelLogger getLogger() {
        return logger;
    }

    abstract protected KeelQueueNextTaskSeeker getNextTaskSeeker();

    public void start() {
        Keel.registerDeployedKeelVerticle(this);

        // 部署之后重新加载一遍
        this.logger = prepareLogger();
        setLogger(this.logger);

        this.queueStatus = QueueStatus.RUNNING;

        routine();
    }

    abstract protected Future<QueueSignal> readSignal();

    protected final void routine() {
        getLogger().debug("KeelQueue::routine start");
        KeelQueueNextTaskSeeker nextTaskSeeker = getNextTaskSeeker();

        readSignal()
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
                        if (getQueueStatus() == QueueStatus.RUNNING) {
                            this.queueStatus = QueueStatus.STOPPED;
                            getLogger().notice("Signal Stop Received");
                        }
                        return Future.succeededFuture();
                    } else if (signal == QueueSignal.RUN) {
                        this.queueStatus = QueueStatus.RUNNING;
                        // 1. seek next task to do
                        return FutureRecursion.call(
                                        true,
                                        shouldNext -> {
                                            if (!shouldNext) {
                                                return Future.succeededFuture(false);
                                            }
                                            return nextTaskSeeker.hasMore();
                                        },
                                        shouldNext -> {
                                            // 尝试寻找
                                            return nextTaskSeeker.seek()
                                                    .compose(task -> {
                                                        if (task == null) {
                                                            // 队列里已经空了，不必再找
                                                            getLogger().debug("No more task todo");
                                                            return Future.succeededFuture(false);
                                                        }
                                                        // 队列里找出来一个task, deploy it (至于能不能跑起来有没有锁就不管了)
                                                        getLogger().info("To run task: " + task.getTaskReference());
                                                        getLogger().info("Trusted that task is already locked by seeker: " + task.getTaskReference());
                                                        return task.deployMeAsWorker()
                                                                .compose(deploymentID -> {
                                                                    getLogger().info("TASK [" + task.getTaskReference() + "] VERTICLE DEPLOYED: " + deploymentID);
                                                                    return Future.succeededFuture(true);
                                                                })
                                                                .recover(throwable -> {
                                                                    getLogger().exception("CANNOT DEPLOY TASK [" + task.getTaskReference() + "] VERTICLE", throwable);
                                                                    return Future.succeededFuture(true);
                                                                });
                                                    });
                                        }
                                )
                                .recover(throwable -> {
                                    getLogger().exception("KeelQueue 递归找活干里出现了奇怪的故障", throwable);
                                    return Future.succeededFuture(false);
                                });
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

    @Override
    public void stop() throws Exception {
        this.queueStatus = QueueStatus.STOPPED;

        Keel.unregisterDeployedKeelVerticle(this.deploymentID());
    }

    public interface KeelQueueNextTaskSeeker {
        Future<Boolean> hasMore();

        /**
         * 找出一个task且其已完成lockTaskBeforeDeployment方法的调用
         *
         */
        Future<KeelQueueTask> seek();

        long waitingMs();
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
