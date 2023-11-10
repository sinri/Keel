package io.github.sinri.keel.test.lab.queue;

import io.github.sinri.keel.servant.queue.KeelQueue;
import io.github.sinri.keel.servant.queue.KeelQueueNextTaskSeeker;
import io.github.sinri.keel.servant.queue.QueueWorkerPoolManager;
import io.vertx.core.Future;

import javax.annotation.Nonnull;

public class TestQueue extends KeelQueue {
    @Nonnull
    @Override
    protected KeelQueueNextTaskSeeker getNextTaskSeeker() {
        return new TestQueueTaskSeeker();
    }

    @Nonnull
    @Override
    protected SignalReader getSignalReader() {
        return new SignalReader() {
            @Override
            public Future<QueueSignal> readSignal() {
                return Future.succeededFuture(QueueSignal.RUN);
            }
        };
    }

    @Nonnull
    @Override
    protected QueueWorkerPoolManager getQueueWorkerPoolManager() {
        return new QueueWorkerPoolManager(3);
    }
}
