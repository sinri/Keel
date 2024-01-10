package io.github.sinri.keel.helper.runtime;

import io.vertx.core.Handler;

import java.util.concurrent.atomic.AtomicReference;

import static io.github.sinri.keel.facade.KeelInstance.Keel;
import static io.github.sinri.keel.helper.KeelHelpersInterface.KeelHelpers;

/**
 * @since 3.0.0
 */
public class KeelRuntimeMonitor {
    private final AtomicReference<GCStatResult> _lastGCRef = new AtomicReference<>();
    private final AtomicReference<CPUTimeResult> _lastCPUTimeRef = new AtomicReference<>();


    public void startRuntimeMonitor(long interval, Handler<MonitorSnapshot> handler) {
        Keel.getVertx().setPeriodic(interval, timer -> {
            MonitorSnapshot monitorSnapshot = new MonitorSnapshot();

            GCStatResult gcSnapshot = KeelHelpers.runtimeHelper().getGCSnapshot();
            CPUTimeResult cpuTimeSnapshot = KeelHelpers.runtimeHelper().getCPUTimeSnapshot();
            MemoryResult memorySnapshot = KeelHelpers.runtimeHelper().getMemorySnapshot();

            GCStatResult lastGC = _lastGCRef.get();
            if (lastGC != null) {
                GCStatResult gcDiff = gcSnapshot.since(lastGC);
                monitorSnapshot.setGCStat(gcDiff);
            } else {
                monitorSnapshot.setGCStat(new GCStatResult());
            }
            _lastGCRef.set(gcSnapshot);

            CPUTimeResult lastCpuTime = this._lastCPUTimeRef.get();
            if (lastCpuTime == null) {
                _lastCPUTimeRef.set(cpuTimeSnapshot);
                monitorSnapshot.setCPUTime(new CPUTimeResult());
            } else {
                CPUTimeResult cpuTimeDiff = cpuTimeSnapshot.since(lastCpuTime);
                monitorSnapshot.setCPUTime(cpuTimeDiff);
            }

            monitorSnapshot.setMemory(memorySnapshot);

            handler.handle(monitorSnapshot);
        });
    }
}
