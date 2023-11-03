package io.github.sinri.keel.servant.queue;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @since 3.0.9
 */
public class QueueWorkerPoolManager {
    private final AtomicInteger maxWorkerCountRef;
    private final AtomicInteger runningWorkerCounter;

    /**
     * @param maxWorkerCount If zero, unlimited workers would be thought.
     */
    public QueueWorkerPoolManager(int maxWorkerCount) {
        this.maxWorkerCountRef = new AtomicInteger(maxWorkerCount);
        this.runningWorkerCounter = new AtomicInteger(0);
    }

    public QueueWorkerPoolManager() {
        this(0);
    }

    public void changeMaxWorkerCount(int maxWorkerCount) {
        this.maxWorkerCountRef.set(maxWorkerCount);
    }

    public boolean isBusy() {
        if (maxWorkerCountRef.get() <= 0) {
            return false;
        }
        return runningWorkerCounter.get() >= maxWorkerCountRef.get();
    }

    public void whenOneWorkerStarts() {
        this.runningWorkerCounter.incrementAndGet();
    }

    public void whenOneWorkerEnds() {
        this.runningWorkerCounter.decrementAndGet();
    }
}
