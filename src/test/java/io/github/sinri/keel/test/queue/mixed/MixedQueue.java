package io.github.sinri.keel.test.queue.mixed;

import io.github.sinri.keel.Keel;
import io.github.sinri.keel.core.logger.KeelLogger;
import io.github.sinri.keel.queue.KeelSyncMixedQueue;
import io.github.sinri.keel.queue.KeelSyncQueueTask;
import io.github.sinri.keel.test.queue.QueueTask;

import java.util.ArrayList;
import java.util.List;

public class MixedQueue extends KeelSyncMixedQueue {
    public MixedQueue(int maxWorkerCount) {
        super(maxWorkerCount);
    }

    public static void main(String[] args) {
        new MixedQueue(3).run();
    }

    @Override
    protected KeelLogger getLogger() {
        return Keel.outputLogger("MixedQueue");
    }

    @Override
    protected boolean shouldStop() {
        return getParallelPool().getTaskCount() > 20 && getSerialPool().getTaskCount() > 10;
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

    @Override
    protected KeelSyncQueueTask getNextSerialTask() {
        double v = Math.random();
        if (v > 0.3) {
            return new QueueTask((int) (getSerialPool().getTaskCount() + 1), v);
        }
        return null;
    }
}
