package io.github.sinri.Keel.servant;

import io.vertx.core.Vertx;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class KeelServantTimer {
    private final Vertx vertx;
    private final long byMinuteTimerID;
    private final Map<String, KeelServantTimerWorker> registeredWorkers = new HashMap<>();

    public KeelServantTimer(Vertx vertx) {
        this.vertx = vertx;
        byMinuteTimerID = this.vertx.setPeriodic(60 * 1000, id -> {
            Calendar calendar = Calendar.getInstance();
            registeredWorkers.forEach((workerName, worker) -> worker.trigger(calendar));
        });
    }

    public KeelServantTimer registerWorker(String workerName, KeelServantTimerWorker worker) {
        registeredWorkers.put(workerName, worker);
        return this;
    }

    public KeelServantTimer registerWorkers(Map<String, KeelServantTimerWorker> map) {
        registeredWorkers.putAll(map);
        return this;
    }

    public KeelServantTimer unregisterWorker(String workerName) {
        registeredWorkers.remove(workerName);
        return this;
    }

    public void stop() {
        this.vertx.cancelTimer(byMinuteTimerID);
    }
}
