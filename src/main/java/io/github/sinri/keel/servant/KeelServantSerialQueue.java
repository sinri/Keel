package io.github.sinri.keel.servant;

import io.github.sinri.keel.Keel;
import io.github.sinri.keel.core.logger.KeelLogger;
import io.vertx.core.Future;

abstract public class KeelServantSerialQueue {

    public KeelServantSerialQueue() {
    }

    abstract public Future<KeelServantQueueTask> getNextTask();

    protected KeelLogger getLogger() {
        return new KeelLogger();
    }

    @Deprecated
    final public Future<Void> run() {
        return getNextTask()
                .compose(KeelServantQueueTask::finalExecute)
                .recover(throwable -> {
                    if (throwable instanceof KeelQueueNoTaskPendingSituation) {
                        // no tasks pending
                        getLogger().debug(getClass() + " run, KeelQueueNoTaskPendingSituation found: " + throwable.getMessage());
                    } else {
                        getLogger().error(getClass() + " run, throwable found: " + throwable.getMessage());
                        getLogger().exception(throwable);
                    }
                    return Future.succeededFuture();
                })
                .eventually(x -> run());
//        return Future.succeededFuture();
    }

    final public void run(long sleepPeriod) {
        Keel.getVertx().setTimer(
                sleepPeriod,
                timerID -> getNextTask()
//                        .onFailure(throwable -> {
//                            if (throwable instanceof KeelQueueNoTaskPendingSituation) {
//                                // no tasks pending
//                                getLogger().debug(getClass() + " run, KeelQueueNoTaskPendingSituation found: " + throwable.getMessage());
//                            } else {
//                                getLogger().error(getClass() + " run, failed in seeking next: " + throwable.getMessage());
//                                getLogger().exception(throwable);
//                            }
//                            run(sleepPeriod);
//                        })
                        .compose(KeelServantQueueTask::finalExecute)
//                        .onFailure(throwable -> {
//                            getLogger().error(getClass() + " run, failed in executing: " + throwable.getMessage());
//                            getLogger().exception(throwable);
//                            run(sleepPeriod);
//                        })
//                        .onSuccess(x -> {
//                            run(1);
//                        })
                        .compose(v -> {
                            run(1);
                            return Future.succeededFuture();
                        })
                        .recover(throwable -> {
                            if (throwable instanceof KeelQueueNoTaskPendingSituation) {
                                // no tasks pending
                                getLogger().debug(getClass() + " run, KeelQueueNoTaskPendingSituation found: " + throwable.getMessage());
                            } else {
                                getLogger().error(getClass() + " run, failed in seeking next: " + throwable.getMessage());
                                getLogger().exception(throwable);
                            }
                            run(sleepPeriod);
                            return Future.succeededFuture();
                        })
        );
    }
}
