package io.github.sinri.keel.helper.runtime;

import io.vertx.core.Handler;

import javax.annotation.Nonnull;
import java.util.concurrent.atomic.AtomicReference;

import static io.github.sinri.keel.facade.KeelInstance.Keel;
import static io.github.sinri.keel.helper.KeelHelpersInterface.KeelHelpers;

/**
 * @since 3.0.0
 * @since 3.1.4 add JVM and Heap Memory Monitoring
 */
public class KeelRuntimeMonitor {
    private final AtomicReference<GCStatResult> _lastGCRef = new AtomicReference<>();
    private final AtomicReference<CPUTimeResult> _lastCPUTimeRef = new AtomicReference<>();


    public void startRuntimeMonitor(long interval, @Nonnull Handler<MonitorSnapshot> handler) {
        // after [interval] waiting, actual snapshots would be taken.
        Keel.getVertx().setPeriodic(interval, timer -> {
            MonitorSnapshot monitorSnapshot = new MonitorSnapshot();

            GCStatResult gcSnapshot = KeelHelpers.runtimeHelper().getGCSnapshot();
            CPUTimeResult cpuTimeSnapshot = KeelHelpers.runtimeHelper().getCPUTimeSnapshot();

            JVMMemoryResult jvmMemoryResultSnapshot = KeelHelpers.runtimeHelper().makeJVMMemorySnapshot();

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

            monitorSnapshot.setJvmMemoryResult(jvmMemoryResultSnapshot);

            handler.handle(monitorSnapshot);
        });
    }
}
