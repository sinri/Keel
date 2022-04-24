package io.github.sinri.keel.syncqueue;

import io.github.sinri.keel.core.logger.KeelLogger;

import java.util.concurrent.*;

@Deprecated
public abstract class KeelSyncSerialQueue {

    private final ThreadPoolExecutor serialPool;

    public KeelSyncSerialQueue() {
        serialPool = new ThreadPoolExecutor(
                1,
                1,
                1000,
                TimeUnit.MILLISECONDS,
                new SynchronousQueue<>(),
                Executors.defaultThreadFactory(),
                new ThreadPoolExecutor.AbortPolicy()
        );
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
            if (serialPool.getActiveCount() > 0) {
                // wait for current task done
                getLogger().info("SERIAL POOL FULL, SLEEP");
                busySleep();
                continue;
            }

            KeelSyncQueueTask nextSerialTask = getNextSerialTask();
            if (nextSerialTask == null) {
                getLogger().info("NO MORE SERIAL TASK, SLEEP");
                busySleep();
                continue;
            }

            getLogger().info("NEXT SERIAL TASK: " + nextSerialTask.getTaskReference());

            if (!nextSerialTask.declareTaskToRun()) {
                getLogger().warning("SERIAL TASK [" + nextSerialTask.getTaskReference() + "] DECLARE TO RUN FAILED, SLEEP");
                busySleep();
                continue;
            }
            try {
                serialPool.execute(nextSerialTask);
            } catch (RejectedExecutionException e) {
                getLogger().warning("RejectedExecutionException: " + e.getMessage());
            }

            busySleep();
        }
        serialPool.shutdown();
        getLogger().notice("SERIAL POOL SHUTDOWN");
    }

    abstract protected KeelSyncQueueTask getNextSerialTask();

}
