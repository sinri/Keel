package io.github.sinri.keel.helper.runtime;

/**
 * @since 2.9.4
 */
public class MonitorSnapshot {
    private GCStatResult GCStat;
    private CPUTimeResult CPUTime;
    private MemoryResult memory;

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

    public MemoryResult getMemory() {
        return memory;
    }

    public MonitorSnapshot setMemory(MemoryResult memory) {
        this.memory = memory;
        return this;
    }
}
