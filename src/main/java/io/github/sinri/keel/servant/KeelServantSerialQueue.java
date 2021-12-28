package io.github.sinri.keel.servant;

import io.github.sinri.keel.Keel;
import io.github.sinri.keel.core.logger.KeelLogger;
import io.vertx.core.Future;

abstract public class KeelServantSerialQueue {

    private final long sleepPeriod;

    public KeelServantSerialQueue(long sleepPeriod) {
        this.sleepPeriod = sleepPeriod;
    }

    abstract public Future<KeelServantQueueTask> getNextTask();

    protected KeelLogger getLogger() {
        return new KeelLogger();
    }

    final protected void runCore(long timerID) {
        getLogger().debug(getClass() + " runCore(" + timerID + ") fired");
        getNextTask()
                .compose(KeelServantQueueTask::finalExecute)
                .onSuccess(v -> {
                    getLogger().debug(getClass() + " runCore(" + timerID + ") success for next runCore");
                    runCore(timerID);
                })
                .onFailure(throwable -> {
                    if (throwable instanceof KeelQueueNoTaskPendingSituation) {
                        // no tasks pending
                        getLogger().debug(getClass() + " run, KeelQueueNoTaskPendingSituation found: " + throwable.getMessage());
                    } else {
                        getLogger().error(getClass() + " run, failed in seeking next: " + throwable.getMessage());
                        getLogger().exception(throwable);
                    }
                    run();
                });
    }

    final public void run() {
        long timer = Keel.getVertx().setTimer(sleepPeriod, this::runCore);
        getLogger().debug(getClass() + " run(" + sleepPeriod + ") set a timer: " + timer);
    }
}
