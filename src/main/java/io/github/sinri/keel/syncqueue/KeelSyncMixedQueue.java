package io.github.sinri.keel.syncqueue;

import io.github.sinri.keel.core.logger.KeelLogger;

import java.util.List;
import java.util.concurrent.*;

/**
 * @since 1.8
 */
@Deprecated
public abstract class KeelSyncMixedQueue {
    private final ThreadPoolExecutor serialPool;
    private final ThreadPoolExecutor parallelPool;

    public KeelSyncMixedQueue(int maxWorkerCount) {
        serialPool = new ThreadPoolExecutor(
                1,
                1,
                1000,
                TimeUnit.MILLISECONDS,
                new SynchronousQueue<>(),
                Executors.defaultThreadFactory(),
                new ThreadPoolExecutor.AbortPolicy()
        );
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

    protected ThreadPoolExecutor getSerialPool() {
        return serialPool;
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
            if (serialRoutine() && parallelRoutine()) {
                getLogger().info("SLEEP");
                busySleep();
            }
        }
        getLogger().notice("POOLS TO SHUTDOWN");
        serialPool.shutdown();
        parallelPool.shutdown();
    }

    abstract protected List<KeelSyncQueueTask> getNextParallelTasks();

    abstract protected KeelSyncQueueTask getNextSerialTask();

    private boolean parallelRoutine() {
        if (parallelPool.getActiveCount() >= parallelPool.getCorePoolSize()) {
            // wait for current task done
            getLogger().info("PARALLEL POOL FULL");
            return true;
        }

        List<KeelSyncQueueTask> nextParallelTasks = getNextParallelTasks();

        if (nextParallelTasks.isEmpty()) {
            getLogger().info("NO MORE PARALLEL TASK");
            return true;
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
                return true;
            }
        }

        return false;
    }

    private boolean serialRoutine() {
        if (serialPool.getActiveCount() > 0) {
            // wait for current task done
            getLogger().info("SERIAL POOL FULL");
            return true;
        }

        KeelSyncQueueTask nextSerialTask = getNextSerialTask();
        if (nextSerialTask == null) {
            getLogger().info("NO MORE SERIAL TASK");
            return true;
        }

        getLogger().info("NEXT SERIAL TASK: " + nextSerialTask.getTaskReference());

        if (!nextSerialTask.declareTaskToRun()) {
            getLogger().warning("SERIAL TASK [" + nextSerialTask.getTaskReference() + "] DECLARE TO RUN FAILED");
            return true;
        }
        try {
            serialPool.execute(nextSerialTask);
        } catch (RejectedExecutionException e) {
            getLogger().warning("RejectedExecutionException: " + e.getMessage());
            return true;
        }

        return false;
    }
}
