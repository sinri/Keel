package io.github.sinri.keel.helper.runtime;

import io.vertx.core.json.JsonObject;

/**
 * @since 3.1.9
 */
public class JVMMemoryResult implements RuntimeStatResult<JVMMemoryResult> {
    private final long statTime;

    private long physicalMaxBytes;
    private long physicalUsedBytes;
    private long runtimeHeapMaxBytes;
    private long runtimeHeapAllocatedBytes;
    private long runtimeHeapUsedBytes;
    private long mxHeapUsedBytes;
    private long mxNonHeapUsedBytes;

    public JVMMemoryResult() {
        this.statTime = System.currentTimeMillis();
    }

    @Override
    public long getStatTime() {
        return statTime;
    }

    public long getRuntimeHeapAllocatedBytes() {
        return runtimeHeapAllocatedBytes;
    }

    public JVMMemoryResult setRuntimeHeapAllocatedBytes(long runtimeHeapAllocatedBytes) {
        this.runtimeHeapAllocatedBytes = runtimeHeapAllocatedBytes;
        return this;
    }

    public long getRuntimeHeapMaxBytes() {
        return runtimeHeapMaxBytes;
    }

    public JVMMemoryResult setRuntimeHeapMaxBytes(long runtimeHeapMaxBytes) {
        this.runtimeHeapMaxBytes = runtimeHeapMaxBytes;
        return this;
    }

    public long getRuntimeHeapUsedBytes() {
        return runtimeHeapUsedBytes;
    }

    public JVMMemoryResult setRuntimeHeapUsedBytes(long runtimeHeapUsedBytes) {
        this.runtimeHeapUsedBytes = runtimeHeapUsedBytes;
        return this;
    }

    public long getMxHeapUsedBytes() {
        return mxHeapUsedBytes;
    }

    public JVMMemoryResult setMxHeapUsedBytes(long mxHeapUsedBytes) {
        this.mxHeapUsedBytes = mxHeapUsedBytes;
        return this;
    }

    public long getMxNonHeapUsedBytes() {
        return mxNonHeapUsedBytes;
    }

    public JVMMemoryResult setMxNonHeapUsedBytes(long mxNonHeapUsedBytes) {
        this.mxNonHeapUsedBytes = mxNonHeapUsedBytes;
        return this;
    }

    public long getPhysicalMaxBytes() {
        return physicalMaxBytes;
    }

    public JVMMemoryResult setPhysicalMaxBytes(long physicalMaxBytes) {
        this.physicalMaxBytes = physicalMaxBytes;
        return this;
    }

    public long getPhysicalUsedBytes() {
        return physicalUsedBytes;
    }

    public JVMMemoryResult setPhysicalUsedBytes(long physicalUsedBytes) {
        this.physicalUsedBytes = physicalUsedBytes;
        return this;
    }

    @Override
    @Deprecated
    public JVMMemoryResult since(JVMMemoryResult start) {
        throw new RuntimeException("DO NOT USE THIS");
    }

    @Override
    public JsonObject toJsonObject() {
        return new JsonObject()
                .put("stat_time", statTime)
                .put("physical_max_bytes", physicalMaxBytes)
                .put("physical_used_bytes", physicalUsedBytes)
                .put("runtime_heap_max_bytes", runtimeHeapMaxBytes)
                .put("runtime_heap_allocated_bytes", runtimeHeapAllocatedBytes)
                .put("runtime_heap_used_bytes", runtimeHeapUsedBytes)
                .put("mx_heap_used_bytes", mxHeapUsedBytes)
                .put("mx_non_heap_used_bytes", mxNonHeapUsedBytes)
                ;
    }
}
