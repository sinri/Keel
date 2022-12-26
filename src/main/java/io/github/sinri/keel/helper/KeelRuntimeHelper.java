package io.github.sinri.keel.helper;

import io.github.sinri.keel.helper.runtime.CPUTimeResult;
import io.github.sinri.keel.helper.runtime.GCStatResult;
import io.github.sinri.keel.helper.runtime.MemoryResult;
import io.vertx.core.json.JsonObject;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.GlobalMemory;

import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.util.Objects;

/**
 * @since 2.9.3
 */
public class KeelRuntimeHelper {
    private static final KeelRuntimeHelper instance = new KeelRuntimeHelper();

    private KeelRuntimeHelper() {

    }

    static KeelRuntimeHelper getInstance() {
        return instance;
    }

    public GCStatResult getGCSnapshot() {
        GCStatResult gcStat = new GCStatResult();
        for (GarbageCollectorMXBean gc : ManagementFactory.getGarbageCollectorMXBeans()) {
            if (Objects.equals("G1 Young Generation", gc.getName())) {
                gcStat.addGCCountAsYoung(gc.getCollectionCount());
                if (gc.getCollectionTime() >= 0) {
                    gcStat.addGCTimeAsYoung(gc.getCollectionTime());
                }
            } else if (Objects.equals("G1 Old Generation", gc.getName())) {
                gcStat.addGCCountAsOld(gc.getCollectionCount());
                if (gc.getCollectionTime() >= 0) {
                    gcStat.addGCTimeAsOld(gc.getCollectionTime());
                }
            } else {
                System.out.println("Found Unknown GarbageCollectorMXBean Name"
                        + new JsonObject()
                        .put("class", gc.getClass().getName())
                        .put("name", gc.getName())
                        .put("memoryPoolNames", KeelHelpers.stringHelper().joinStringArray(gc.getMemoryPoolNames(), ","))
                        .put("objectName", gc.getObjectName())
                        .put("collectionCount", gc.getCollectionCount())
                        .put("collectionTime", gc.getCollectionTime())
                );
            }
        }
        return gcStat;
    }

    /**
     * @since 2.9.4
     */
    public CPUTimeResult getCPUTimeSnapshot() {
        SystemInfo systemInfo = new SystemInfo();
        CentralProcessor processor = systemInfo.getHardware().getProcessor();
        long[] systemCpuLoadTicks = processor.getSystemCpuLoadTicks();

        return new CPUTimeResult()
                .setSpentInUserState(systemCpuLoadTicks[CentralProcessor.TickType.USER.getIndex()])
                .setSpentInNiceState(systemCpuLoadTicks[CentralProcessor.TickType.NICE.getIndex()])
                .setSpentInSystemState(systemCpuLoadTicks[CentralProcessor.TickType.SYSTEM.getIndex()])
                .setSpentInIdleState(systemCpuLoadTicks[CentralProcessor.TickType.IDLE.getIndex()])
                .setSpentInIOWaitState(systemCpuLoadTicks[CentralProcessor.TickType.IOWAIT.getIndex()])
                .setSpentInIRQState(systemCpuLoadTicks[CentralProcessor.TickType.IRQ.getIndex()])
                .setSpentInSoftIRQState(systemCpuLoadTicks[CentralProcessor.TickType.SOFTIRQ.getIndex()])
                .setSpentInStealState(systemCpuLoadTicks[CentralProcessor.TickType.STEAL.getIndex()]);
    }

    /**
     * @since 2.9.4
     */
    public MemoryResult getMemorySnapshot() {
        SystemInfo systemInfo = new SystemInfo();
        GlobalMemory memory = systemInfo.getHardware().getMemory();
        long totalByte = memory.getTotal();
        long availableByte = memory.getAvailable();

        return new MemoryResult()
                .setTotalByte(totalByte)
                .setAvailableByte(availableByte);
    }

}
