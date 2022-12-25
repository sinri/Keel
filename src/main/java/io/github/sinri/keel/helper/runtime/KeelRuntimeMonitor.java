package io.github.sinri.keel.helper.runtime;

import io.github.sinri.keel.facade.Keel;
import io.github.sinri.keel.helper.KeelHelpers;
import io.vertx.core.Handler;

import java.util.concurrent.atomic.AtomicReference;

/**
 * @since 3.0.0
 */
public class KeelRuntimeMonitor {
    private final AtomicReference<GCStatResult> _lastGCRef = new AtomicReference<>();
    private final AtomicReference<CPUTimeResult> _lastCPUTimeRef = new AtomicReference<>();
    private final Keel keel;

    public KeelRuntimeMonitor(Keel keel) {
        this.keel = keel;
    }

    public void startRuntimeMonitor(long interval, Handler<MonitorSnapshot> handler) {
        this.keel.setPeriodic(interval, timer -> {
            MonitorSnapshot monitorSnapshot = new MonitorSnapshot();

            GCStatResult gcSnapshot = KeelHelpers.getInstance().runtimeHelper().getGCSnapshot();
            CPUTimeResult cpuTimeSnapshot = KeelHelpers.getInstance().runtimeHelper().getCPUTimeSnapshot();
            MemoryResult memorySnapshot = KeelHelpers.getInstance().runtimeHelper().getMemorySnapshot();

            GCStatResult lastGC = _lastGCRef.get();
            if (lastGC == null) {
                _lastGCRef.set(gcSnapshot);
            } else {
                GCStatResult gcDiff = gcSnapshot.since(lastGC);
                monitorSnapshot.setGCStat(gcDiff);
            }

            CPUTimeResult lastCpuTime = this._lastCPUTimeRef.get();
            if (lastCpuTime == null) {
                _lastCPUTimeRef.set(cpuTimeSnapshot);
            } else {
                CPUTimeResult cpuTimeDiff = cpuTimeSnapshot.since(lastCpuTime);
                monitorSnapshot.setCPUTime(cpuTimeDiff);
            }

            monitorSnapshot.setMemory(memorySnapshot);

            handler.handle(monitorSnapshot);
        });
    }
}
