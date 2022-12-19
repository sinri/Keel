package io.github.sinri.keel.helper;

import io.github.sinri.keel.facade.interfaces.KeelTraitForVertx;
import io.github.sinri.keel.helper.runtime.CPUTimeResult;
import io.github.sinri.keel.helper.runtime.GCStatResult;
import io.github.sinri.keel.helper.runtime.MemoryResult;
import io.github.sinri.keel.helper.runtime.MonitorSnapshot;
import io.vertx.core.Handler;

import java.util.concurrent.atomic.AtomicReference;

public interface KeelTraitForRuntimeMonitor extends KeelTraitForVertx, TraitForHelpers {
    AtomicReference<GCStatResult> _lastGCRef = new AtomicReference<>();
    AtomicReference<CPUTimeResult> _lastCPUTimeRef = new AtomicReference<>();

    default void startRuntimeMonitor(long interval, Handler<MonitorSnapshot> handler) {
        getVertx().setPeriodic(interval, timer -> {
            MonitorSnapshot monitorSnapshot = new MonitorSnapshot();

            GCStatResult gcSnapshot = runtimeHelper().getGCSnapshot();
            CPUTimeResult cpuTimeSnapshot = runtimeHelper().getCPUTimeSnapshot();
            MemoryResult memorySnapshot = runtimeHelper().getMemorySnapshot();

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
