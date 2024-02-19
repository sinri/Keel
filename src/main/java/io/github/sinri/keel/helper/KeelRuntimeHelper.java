package io.github.sinri.keel.helper;

import io.github.sinri.keel.helper.runtime.CPUTimeResult;
import io.github.sinri.keel.helper.runtime.GCStatResult;
import io.github.sinri.keel.helper.runtime.JVMMemoryResult;
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

    private final SystemInfo systemInfo;

    private KeelRuntimeHelper() {
        systemInfo = new SystemInfo();
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
    @Deprecated(since = "3.1.9")
    public MemoryResult getHardwareMemorySnapshot() {
        GlobalMemory memory = systemInfo.getHardware().getMemory();
        long totalByte = memory.getTotal();
        long availableByte = memory.getAvailable();

        return new MemoryResult()
                .setTotalByte(totalByte)
                .setAvailableByte(availableByte);
    }

    /**
     * @since 3.1.4 这里的口径似乎是JVM内实际申请占用的内存。
     * @since 3.1.9 让我们换一下口径： Total = Xmx; Available = Free+Max-Total
     */
    @Nonnull
    @Deprecated(since = "3.1.9")
    public MemoryResult getJVMMemorySnapshot() {
        Runtime runtime = runtime();
        long maxMemory = runtime.maxMemory();// JVM 会试图使用的内存总量（如通过Xmx设置或自动设置）
        long totalMemory = runtime.totalMemory(); // JVM总内存量（当前实际申请下来的）
        long freeMemory = runtime.freeMemory(); // JVM空闲内存量（当前实际申请下来的内存但没使用或已释放的）
        // modified since 3.1.9
//        return new MemoryResult()
//                .setTotalByte(totalMemory)
//                .setAvailableByte(freeMemory);
        // since 3.1.9
        return new MemoryResult()
                .setTotalByte(maxMemory)
                .setAvailableByte(freeMemory + maxMemory - totalMemory);
    }

    /**
     * @since 3.1.4
     * 这里的口径似乎是JVM内名义上占用的内存。
     */
    @Nonnull
    @Deprecated(since = "3.1.9")
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

    public JVMMemoryResult makeJVMMemorySnapshot() {
        Runtime runtime = runtime();
        long maxMemory = runtime.maxMemory();// JVM 会试图使用的内存总量（如通过Xmx设置或自动设置）
        long totalMemory = runtime.totalMemory(); // JVM总内存量（当前实际申请下来的）
        long freeMemory = runtime.freeMemory(); // JVM空闲内存量（当前实际申请下来的内存但没使用或已释放的）

        GlobalMemory memory = systemInfo.getHardware().getMemory();

        // freeMemory + maxMemory - totalMemory
        return new JVMMemoryResult()
                .setPhysicalMaxBytes(memory.getTotal())
                .setPhysicalUsedBytes(memory.getTotal() - memory.getAvailable())
                .setRuntimeHeapMaxBytes(maxMemory)
                .setRuntimeHeapAllocatedBytes(totalMemory)
                .setRuntimeHeapUsedBytes(totalMemory - freeMemory)
                .setMxHeapUsedBytes(getHeapMemoryUsage().getUsed())
                .setMxNonHeapUsedBytes(getNonHeapMemoryUsage().getUsed()) // 独立的
                ;
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
