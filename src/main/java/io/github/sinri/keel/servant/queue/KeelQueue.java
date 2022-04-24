package io.github.sinri.keel.servant.queue;

import io.github.sinri.keel.Keel;
import io.github.sinri.keel.core.controlflow.FutureRecursion;
import io.github.sinri.keel.core.logger.KeelLogger;
import io.github.sinri.keel.verticles.KeelVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;

public abstract class KeelQueue extends KeelVerticle {
    private long waitingSeconds = 1000L;

    public KeelQueue() {
        super();
        setLogger(prepareLogger());
    }

    public KeelQueue setWaitingSeconds(long waitingSeconds) {
        this.waitingSeconds = waitingSeconds;
        return this;
    }

    abstract protected KeelLogger prepareLogger();

    abstract protected KeelQueueNextTaskSeeker getNextTaskSeeker();

    public void start() {
        Keel.getVertx().setTimer(waitingSeconds, timerID -> {
            routine();
        });
    }

    protected final void routine() {
        getLogger().debug("KeelQueue::routine start");

        KeelQueueNextTaskSeeker nextTaskSeeker = getNextTaskSeeker();
        // 1. seek next task to do
        new FutureRecursion<Boolean>(
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
                                    return Future.succeededFuture(false);
                                }
                                // 队列里找出来一个task, deploy it (至于能不能跑起来有没有锁就不管了)
                                Keel.getVertx().deployVerticle(
                                                task,
                                                new DeploymentOptions()
                                                        .setWorker(true)
                                        )
                                        .compose(deploymentID -> {
                                            getLogger().warning("TASK VERTICLE DEPLOYED: " + deploymentID);
                                            return Future.succeededFuture(true);
                                        });

                                // 继续找
                                return Future.succeededFuture(true);
                            });
                }
        )
                .run(true)
                .recover(throwable -> {
                    getLogger().exception("KeelQueue 递归找活干里出现了奇怪的故障", throwable);
                    return Future.succeededFuture(false);
                })
                .eventually(v -> {
                    Keel.getVertx().setTimer(waitingSeconds, timerID -> {
                        routine();
                    });
                    return Future.succeededFuture();
                });
    }

    public interface KeelQueueNextTaskSeeker {
        Future<Boolean> hasMore();

        Future<KeelQueueTask> seek();
    }
}
