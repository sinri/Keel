package io.github.sinri.keel.test.lab.queue;

import io.github.sinri.keel.servant.queue.KeelQueue;
import io.github.sinri.keel.servant.queue.KeelQueueNextTaskSeeker;
import io.github.sinri.keel.servant.queue.QueueWorkerPoolManager;
import io.vertx.core.Future;
import org.jetbrains.annotations.NotNull;

public class TestQueue extends KeelQueue {
    @NotNull
    @Override
    protected KeelQueueNextTaskSeeker getNextTaskSeeker() {
        return new TestQueueTaskSeeker();
    }

    @NotNull
    @Override
    protected SignalReader getSignalReader() {
        return new SignalReader() {
            @Override
            public Future<QueueSignal> readSignal() {
                return Future.succeededFuture(QueueSignal.RUN);
            }
        };
    }

    @NotNull
    @Override
    protected QueueWorkerPoolManager getQueueWorkerPoolManager() {
        return new QueueWorkerPoolManager(3);
    }
}
