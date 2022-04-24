package io.github.sinri.keel.syncqueue;

import io.github.sinri.keel.core.logger.KeelLogger;

import java.util.List;
import java.util.concurrent.*;

@Deprecated
public abstract class KeelSyncParallelQueue {
    private final ThreadPoolExecutor parallelPool;

    public KeelSyncParallelQueue(int maxWorkerCount) {
        this.parallelPool = new ThreadPoolExecutor(
                maxWorkerCount,
                maxWorkerCount,
                1000,
                TimeUnit.MILLISECONDS,
                new SynchronousQueue<>(),
                Executors.defaultThreadFactory(),
                new ThreadPoolExecutor.AbortPolicy()
        );
    }

    protected ThreadPoolExecutor getParallelPool() {
        return parallelPool;
    }

    abstract protected KeelLogger getLogger();

    abstract protected boolean shouldStop();

    protected long getBusyWaitMilliSeconds() {
        return 1000 * 5;
    }

    private void busySleep() {
        try {
            Thread.sleep(getBusyWaitMilliSeconds());
        } catch (InterruptedException e) {
            getLogger().exception(e);
        }
    }

    public void run() {
        while (!shouldStop()) {
            if (parallelPool.getActiveCount() >= parallelPool.getCorePoolSize()) {
                // wait for current task done
                getLogger().info("PARALLEL POOL FULL, SLEEP");
                busySleep();
                continue;
            }

            List<KeelSyncQueueTask> nextParallelTasks = getNextParallelTasks();

            if (nextParallelTasks.isEmpty()) {
                getLogger().info("NO MORE PARALLEL TASK, SLEEP");
                busySleep();
                continue;
            }

            for (var task : nextParallelTasks) {
                if (task == null) continue;
                getLogger().info("NEXT PARALLEL TASK: " + task.getTaskReference());

                if (!task.declareTaskToRun()) {
                    getLogger().warning("PARALLEL TASK [" + task.getTaskReference() + "] DECLARE TO RUN FAILED, PASSOVER");
                    continue;
                }

                try {
                    parallelPool.execute(task);
                } catch (RejectedExecutionException e) {
                    getLogger().warning("RejectedExecutionException: " + e.getMessage());
                    break;
                }
            }
        }
        parallelPool.shutdown();
        getLogger().notice("PARALLEL POOL SHUTDOWN");
    }

    abstract protected List<KeelSyncQueueTask> getNextParallelTasks();
}
