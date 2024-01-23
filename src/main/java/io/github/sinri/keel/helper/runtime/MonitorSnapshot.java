package io.github.sinri.keel.helper.runtime;

/**
 * @since 2.9.4
 */
public class MonitorSnapshot {
    private GCStatResult GCStat;
    private CPUTimeResult CPUTime;
    private MemoryResult hardwareMemory;
    /**
     * @since 3.1.4
     */
    private MemoryResult jvmMemory;
    /**
     * @since 3.1.4
     */
    private MemoryResult jvmHeapMemory;

    public CPUTimeResult getCPUTime() {
        return CPUTime;
    }

    public MonitorSnapshot setCPUTime(CPUTimeResult CPUTime) {
        this.CPUTime = CPUTime;
        return this;
    }

    public GCStatResult getGCStat() {
        return GCStat;
    }

    public MonitorSnapshot setGCStat(GCStatResult GCStat) {
        this.GCStat = GCStat;
        return this;
    }

    /**
     * @since 3.1.4
     */
    public MemoryResult getHardwareMemory() {
        return hardwareMemory;
    }

    /**
     * @since 3.1.4
     */
    public MonitorSnapshot setHardwareMemory(MemoryResult memory) {
        this.hardwareMemory = memory;
        return this;
    }

    /**
     * @since 3.1.4
     */
    public MemoryResult getJvmHeapMemory() {
        return jvmHeapMemory;
    }

    /**
     * @since 3.1.4
     */
    public MonitorSnapshot setJvmHeapMemory(MemoryResult jvmHeapMemory) {
        this.jvmHeapMemory = jvmHeapMemory;
        return this;
    }

    /**
     * @since 3.1.4
     */
    public MemoryResult getJvmMemory() {
        return jvmMemory;
    }

    /**
     * @since 3.1.4
     */
    public MonitorSnapshot setJvmMemory(MemoryResult jvmMemory) {
        this.jvmMemory = jvmMemory;
        return this;
    }
}
