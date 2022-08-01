package io.github.sinri.keel.servant.sundial;

import io.github.sinri.keel.Keel;
import io.github.sinri.keel.core.controlflow.FutureForEach;
import io.github.sinri.keel.core.logger.KeelLogger;
import io.vertx.core.Future;
import io.vertx.core.shareddata.Counter;

import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * 日晷 (CRON TABLE)
 *
 * @since 2.7 rename from KeelServantTimer
 */
public class KeelSundial {
    private final KeelLogger logger;
    private final long byMinuteTimerID;
    private final Map<String, KeelSundialWorker> registeredWorkers = new HashMap<>();

    public KeelSundial(KeelLogger logger) {
        this.logger = logger;

        byMinuteTimerID = Keel.getVertx().setPeriodic(60 * 1000, id -> {
            Calendar calendar = Calendar.getInstance();

            FutureForEach.call(
                    registeredWorkers.keySet(),
                    workerName -> {
                        KeelSundialWorker worker = registeredWorkers.get(workerName);
                        if (!worker.shouldRunNow(calendar)) {
                            return Future.succeededFuture();
                        }
                        int parallelLimit = worker.getParallelLimit();
                        if (parallelLimit > 0) {
                            return this.getWorkerParallelCounter(workerName).compose(counter -> {
                                        return counter.get().compose(current -> {
                                            if (current < parallelLimit) {
                                                counter.incrementAndGet(x -> {
                                                    getLogger().info("Worker Name " + workerName + " to work");
                                                    worker.work(calendar).onComplete(asyncResult -> {
                                                        counter.decrementAndGet();
                                                    });
                                                });
                                            } else {
                                                getLogger().warning("Worker Name " + workerName + " current parallel " + current + " >= limit " + parallelLimit);
                                            }
                                            return Future.succeededFuture();
                                        });
                                    })
                                    .recover(throwable -> {
                                        getLogger().exception(throwable);
                                        return Future.succeededFuture();
                                    })
                                    .compose(v -> {
                                        return Future.succeededFuture();
                                    });
                        } else {
                            worker.work(calendar);
                            return Future.succeededFuture();
                        }
                    }
            );
        });
    }

    public KeelLogger getLogger() {
        return logger;
    }

    private Future<Counter> getWorkerParallelCounter(String workerName) {
        return Keel.getVertx().sharedData()
                .getCounter("KeelSundial-" + workerName);
    }

    public KeelSundial registerWorker(KeelSundialWorker worker) {
        registeredWorkers.put(worker.getName(), worker);
        getLogger().info("register worker: " + worker.getName() + " for " + worker.getParsedCronExpression());
        return this;
    }

    public KeelSundial registerWorkers(Collection<KeelSundialWorker> workers) {
        workers.forEach(this::registerWorker);
        return this;
    }

    public KeelSundial unregisterWorker(String workerName) {
        registeredWorkers.remove(workerName);
        getLogger().info("unregister worker: " + workerName);
        return this;
    }

    public void stop() {
        boolean done = Keel.getVertx().cancelTimer(byMinuteTimerID);
        getLogger().info("stopped timer " + byMinuteTimerID + ": " + done);
    }
}
