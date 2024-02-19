package io.github.sinri.keel.helper.runtime;

/**
 * @since 2.9.4
 */
public class MonitorSnapshot {
    private GCStatResult GCStat;
    private CPUTimeResult CPUTime;
    /**
     * @since 3.1.9
     */
    private JVMMemoryResult jvmMemoryResult;

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
     * @since 3.1.9
     */
    public JVMMemoryResult getJvmMemoryResult() {
        return jvmMemoryResult;
    }

    /**
     * @since 3.1.9
     */
    public MonitorSnapshot setJvmMemoryResult(JVMMemoryResult jvmMemoryResult) {
        this.jvmMemoryResult = jvmMemoryResult;
        return this;
    }
}
