package io.github.sinri.keel.servant;

import io.vertx.core.Future;

abstract public class KeelServantSerialQueue {

    public KeelServantSerialQueue() {
    }

    abstract public Future<KeelServantQueueTask> getNextTask();

    final public Future<Void> run() {
        getNextTask().compose(KeelServantQueueTask::finalExecute)
                .eventually(x -> run());
        return Future.succeededFuture();
    }
}
