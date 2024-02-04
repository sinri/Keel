package io.github.sinri.keel.helper;

import io.github.sinri.keel.helper.runtime.CPUTimeResult;
import io.github.sinri.keel.helper.runtime.GCStatResult;
import io.github.sinri.keel.helper.runtime.MemoryResult;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.GlobalMemory;

import javax.annotation.Nonnull;
import java.lang.management.*;
import java.util.Set;

/**
 * @since 2.9.3
 * @since 3.1.3 Add more GarbageCollectorMXBean.
 */
public class KeelRuntimeHelper {
    private static final KeelRuntimeHelper instance = new KeelRuntimeHelper();

    public static final Set<String> ignorableCallStackPackage;

    static {
        ignorableCallStackPackage = Set.of(
                "io.github.sinri.keel.facade.async.",
                "io.github.sinri.keel.tesuto.",
                "io.vertx.core.",
                "io.netty.",
                "java.lang.",
                "jdk.internal."
        );
    }


    private KeelRuntimeHelper() {

    }

    static KeelRuntimeHelper getInstance() {
        return instance;
    }

    private OperatingSystemMXBean osMX() {
        return ManagementFactory.getOperatingSystemMXBean();
    }

    private MemoryMXBean memoryMX() {
        return ManagementFactory.getMemoryMXBean();
    }

    private Runtime runtime() {
        return Runtime.getRuntime();
    }

    @Nonnull
    public GCStatResult getGCSnapshot() {
        GCStatResult gcStat = new GCStatResult();
        for (GarbageCollectorMXBean gc : ManagementFactory.getGarbageCollectorMXBeans()) {
            //Objects.requireNonNull(gc);
            if (gc == null) {
                continue;
            }
            gcStat.refreshWithGC(gc);
        }
        return gcStat;
    }

    /**
     * @since 2.9.4
     */
    @Nonnull
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
     * @since 2.9.4 named as getMemorySnapshot
     * @since 3.1.4 renamed to getHardwareMemorySnapshot
     */
    @Nonnull
    public MemoryResult getHardwareMemorySnapshot() {
        SystemInfo systemInfo = new SystemInfo();
        GlobalMemory memory = systemInfo.getHardware().getMemory();
        long totalByte = memory.getTotal();
        long availableByte = memory.getAvailable();

        return new MemoryResult()
                .setTotalByte(totalByte)
                .setAvailableByte(availableByte);
    }

    /**
     * @since 3.1.4
     * 这里的口径似乎是JVM内实际申请占用的内存。
     */
    @Nonnull
    public MemoryResult getJVMMemorySnapshot() {
        Runtime runtime = runtime();
        long totalMemory = runtime.totalMemory(); // JVM总内存量
        long freeMemory = runtime.freeMemory(); // JVM空闲内存量
        // long usedMemory = totalMemory - freeMemory; // 使用的内存量
        return new MemoryResult()
                .setTotalByte(totalMemory)
                .setAvailableByte(freeMemory);
    }

    /**
     * @since 3.1.4
     * 这里的口径似乎是JVM内名义上占用的内存。
     */
    @Nonnull
    public MemoryResult getJVMHeapMemorySnapshot() {
        MemoryUsage heapMemoryUsage = getHeapMemoryUsage();
        //long initMemory = heapMemoryUsage.getInit();  // 初始的总内存
        long usedMemory = heapMemoryUsage.getUsed();  // 已使用的内存
        //long committedMemory = heapMemoryUsage.getCommitted();  // 已申请的内存
        long maxMemory = heapMemoryUsage.getMax();  // 最大可用内存

        return new MemoryResult()
                .setTotalByte(maxMemory)
                .setAvailableByte(maxMemory - usedMemory);
    }

    /**
     * Returns the system load average for the last minute. The system load average is the sum of the number of runnable entities queued to the available processors and the number of runnable entities running on the available processors averaged over a period of time. The way in which the load average is calculated is operating system specific but is typically a damped time-dependent average.
     * If the load average is not available, a negative value is returned.
     * This method is designed to provide a hint about the system load and may be queried frequently. The load average may be unavailable on some platform where it is expensive to implement this method.
     *
     * @return the system load average; or a negative value if not available.
     * @since 3.1.4
     */
    public double getSystemLoadAverage() {
        return this.osMX().getSystemLoadAverage();
    }

    /**
     * @since 3.1.4
     */
    public MemoryUsage getHeapMemoryUsage() {
        return this.memoryMX().getHeapMemoryUsage();
    }

    /**
     * @since 3.1.4
     */
    public MemoryUsage getNonHeapMemoryUsage() {
        return this.memoryMX().getNonHeapMemoryUsage();
    }

    /**
     * @since 3.1.4
     */
    public int getObjectPendingFinalizationCount() {
        return this.memoryMX().getObjectPendingFinalizationCount();
    }
}
