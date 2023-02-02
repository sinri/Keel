package io.github.sinri.keel.helper.runtime;

import io.vertx.core.json.JsonObject;

import java.text.DecimalFormat;

/**
 * @since 2.9.4
 */
public class MemoryResult implements RuntimeStatResult<MemoryResult> {
    private final long statTime;
    private long totalByte;
    private long availableByte;

    public MemoryResult() {
        statTime = System.currentTimeMillis();
    }

    private MemoryResult(long statTime) {
        this.statTime = statTime;
    }

    public long getAvailableByte() {
        return availableByte;
    }

    public MemoryResult setAvailableByte(long availableByte) {
        this.availableByte = availableByte;
        return this;
    }

    public long getTotalByte() {
        return totalByte;
    }

    public MemoryResult setTotalByte(long totalByte) {
        this.totalByte = totalByte;
        return this;
    }

    @Override
    public long getStatTime() {
        return statTime;
    }

    @Override
    public MemoryResult since(MemoryResult start) {
        return new MemoryResult(getStatTime())
                .setAvailableByte(getAvailableByte() - start.getAvailableByte())
                .setTotalByte(getTotalByte());
    }

    public double getMemoryUsage() {
        return 1.0 - 1.0 * getAvailableByte() / getTotalByte();
    }

    public String getMemoryUsagePercent() {
        return new DecimalFormat("#.##").format(getMemoryUsage() * 100);
    }

    @Override
    public JsonObject toJsonObject() {
        return new JsonObject()
                .put("stat_time", getStatTime())
                .put("total_bytes", getTotalByte())
                .put("available_bytes", getAvailableByte())
                .put("usage", getMemoryUsagePercent());
    }
}
