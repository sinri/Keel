package io.github.sinri.keel.test.lab.queue;

import io.github.sinri.keel.servant.queue.KeelQueueNextTaskSeeker;
import io.github.sinri.keel.servant.queue.KeelQueueTask;
import io.vertx.core.Future;

import java.util.UUID;

public class TestQueueTaskSeeker implements KeelQueueNextTaskSeeker {
    @Override
    public Future<KeelQueueTask> get() {
        return Future.succeededFuture()
                .compose(v -> {
                    int rest = (int) (10 * Math.random());
                    return Future.succeededFuture(new TestQueueTask(
                            UUID.randomUUID().toString(),
                            rest
                    ));
                });
    }
}
