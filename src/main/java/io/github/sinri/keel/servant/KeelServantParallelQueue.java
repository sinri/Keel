package io.github.sinri.keel.servant;

import io.github.sinri.keel.Keel;
import io.github.sinri.keel.core.logger.KeelLogger;
import io.github.sinri.keel.core.semaphore.KeelShareDataSemaphore;
import io.vertx.core.Future;

import java.util.List;

/**
 * @since 1.3
 */
public abstract class KeelServantParallelQueue {

    private final long sleepPeriod;
    private final KeelShareDataSemaphore keelShareDataSemaphore;
    private final KeelLogger logger;

    public KeelServantParallelQueue(long sleepPeriod, int maxParallelSize, KeelLogger logger) {
        this.sleepPeriod = sleepPeriod;
        this.logger = logger;
        this.keelShareDataSemaphore = new KeelShareDataSemaphore(getClass().getName(), maxParallelSize, this.logger);
    }

    public KeelLogger getLogger() {
        return logger;
    }

    /**
     * @param limit how many tasks to be fetched for once?
     * @return the success future with fetched tasks, or any failed future caused by KeelQueueNoTaskPendingSituation or others
     */
    abstract public Future<List<KeelServantQueueTask>> getNextTasks(long limit);

    final public void run() {
        run(true);
    }

    final public void run(boolean batchSeek) {
        Keel.getVertx().setTimer(sleepPeriod, timerID -> {
            runOneTrail(batchSeek).compose(goForNext -> {
                run(batchSeek);
                return Future.succeededFuture();
            });
        });
    }

    final protected Future<Boolean> runOneTrail(boolean batchSeek) {
        return keelShareDataSemaphore.getAvailablePermits()
                .compose(availablePermits -> {
                    if (availablePermits > 0) {
                        if (!batchSeek) {
                            availablePermits = 1L;
                        }
                    }
                    if (availablePermits > 0) {
                        // do something
                        return getNextTasks(availablePermits)
                                .compose(keelServantQueueTaskList -> {
                                    // let another do it and let main seek next
                                    for (var task : keelServantQueueTaskList) {
                                        task.finalExecute();
                                    }
                                    return Future.succeededFuture(true);
                                })
                                .recover(throwable -> {
                                    if (throwable instanceof KeelQueueNoTaskPendingSituation) {
                                        // no tasks pending
                                        getLogger().debug(getClass() + " run, KeelQueueNoTaskPendingSituation found: " + throwable.getMessage());
                                    } else {
                                        getLogger().error(getClass() + " run, failed in seeking next: " + throwable.getMessage());
                                        getLogger().exception(throwable);
                                    }
                                    // let main sleep
                                    return Future.succeededFuture(false);
                                });
                    } else {
                        // let main sleep
                        return Future.succeededFuture(false);
                    }
                });
    }
}
