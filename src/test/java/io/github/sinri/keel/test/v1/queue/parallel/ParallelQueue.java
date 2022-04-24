package io.github.sinri.keel.test.v1.queue.parallel;

import io.github.sinri.keel.Keel;
import io.github.sinri.keel.core.logger.KeelLogger;
import io.github.sinri.keel.syncqueue.KeelSyncParallelQueue;
import io.github.sinri.keel.syncqueue.KeelSyncQueueTask;
import io.github.sinri.keel.test.v1.queue.QueueTask;

import java.util.ArrayList;
import java.util.List;

public class ParallelQueue extends KeelSyncParallelQueue {
    public ParallelQueue(int maxWorkerCount) {
        super(maxWorkerCount);
    }

    public static void main(String[] args) {
        new ParallelQueue(3).run();
    }

    @Override
    protected KeelLogger getLogger() {
        return Keel.outputLogger("ParallelQueue");
    }

    @Override
    protected boolean shouldStop() {
        return getParallelPool().getTaskCount() > 20;
    }

    @Override
    protected List<KeelSyncQueueTask> getNextParallelTasks() {
        int x = getParallelPool().getCorePoolSize();
        List<KeelSyncQueueTask> list = new ArrayList<>();
        for (int i = 0; i < x; i++) {
            double v = Math.random();
            if (v > 0.3) {
                list.add(new QueueTask((int) (getParallelPool().getTaskCount() + 1), v));
            }
        }
        return list;
    }
}
