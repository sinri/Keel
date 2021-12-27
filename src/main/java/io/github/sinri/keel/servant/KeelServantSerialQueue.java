package io.github.sinri.keel.servant;

import io.github.sinri.keel.core.logger.KeelLogger;
import io.vertx.core.Future;

abstract public class KeelServantSerialQueue {

    public KeelServantSerialQueue() {
    }

    abstract public Future<KeelServantQueueTask> getNextTask();

    protected KeelLogger getLogger() {
        return new KeelLogger();
    }

    final public Future<Void> run() {
        return getNextTask()
                .compose(KeelServantQueueTask::finalExecute)
                .recover(throwable -> {
                    getLogger().error(getClass() + " run, throwable found: " + throwable.getMessage());
                    getLogger().exception(throwable);
                    return Future.succeededFuture();
                })
                .eventually(x -> run());
//        return Future.succeededFuture();
    }
}
