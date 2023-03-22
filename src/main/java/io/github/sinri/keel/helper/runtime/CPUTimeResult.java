package io.github.sinri.keel.helper.runtime;

import io.vertx.core.json.JsonObject;

import java.text.DecimalFormat;

/**
 * <p>
 * Get System-wide CPU Load tick counters.
 * Contains eight items to represent milliseconds spent in
 * User (0),
 * Nice (1),
 * System (2),
 * Idle (3),
 * IOwait (4),
 * Hardware interrupts (IRQ) (5),
 * Software interrupts/DPC (SoftIRQ) (6),
 * or Steal (7) states.
 * </p><p>
 * By measuring the difference between ticks across a time interval,
 * CPU load over that interval may be calculated.
 * </p><p>
 * On some operating systems with variable numbers of logical processors,
 * the size of this array could change and may not align with other per-processor methods.
 * </p><p>
 * Note that while tick counters are in units of milliseconds,
 * they may advance in larger increments along with (platform dependent) clock ticks.
 * For example, by default Windows clock ticks are 1/64 of a second (about 15 or 16 milliseconds)
 * and Linux ticks are distribution and configuration dependent but usually 1/100 of a second (10 milliseconds).
 * Nice and IOWait information is not available on Windows,
 * and IOwait and IRQ information is not available on macOS, so these ticks will always be zero.
 * To calculate overall Idle time using this method,
 * include both Idle and IOWait ticks.
 * Similarly, IRQ, SoftIRQ, and Steal ticks should be added to the System value to get the total.
 * System ticks also include time executing other virtual hosts (steal).
 * </p>
 *
 * @since 2.9.4
 */
public class CPUTimeResult implements RuntimeStatResult<CPUTimeResult> {
    private final long statTime;
    private long spentInUserState = 0;
    private long spentInNiceState = 0;
    private long spentInSystemState = 0;
    private long spentInIdleState = 0;
    private long spentInIOWaitState = 0;
    /**
     * Hardware interrupts (IRQ)
     */
    private long spentInIRQState = 0;
    /**
     * Software interrupts/DPC (SoftIRQ)
     */
    private long spentInSoftIRQState = 0;
    private long spentInStealState = 0;

    public CPUTimeResult() {
        statTime = System.currentTimeMillis();
    }

    private CPUTimeResult(long statTime) {
        this.statTime = statTime;
    }

    public long getSpentInUserState() {
        return spentInUserState;
    }

    public CPUTimeResult setSpentInUserState(long spentInUserState) {
        this.spentInUserState = spentInUserState;
        return this;
    }

    public long getSpentInNiceState() {
        return spentInNiceState;
    }

    public CPUTimeResult setSpentInNiceState(long spentInNiceState) {
        this.spentInNiceState = spentInNiceState;
        return this;
    }

    public long getSpentInSystemState() {
        return spentInSystemState;
    }

    public CPUTimeResult setSpentInSystemState(long spentInSystemState) {
        this.spentInSystemState = spentInSystemState;
        return this;
    }

    public long getSpentInIdleState() {
        return spentInIdleState;
    }

    public CPUTimeResult setSpentInIdleState(long spentInIdleState) {
        this.spentInIdleState = spentInIdleState;
        return this;
    }

    public long getSpentInIOWaitState() {
        return spentInIOWaitState;
    }

    public CPUTimeResult setSpentInIOWaitState(long spentInIOWaitState) {
        this.spentInIOWaitState = spentInIOWaitState;
        return this;
    }

    public long getSpentInIRQState() {
        return spentInIRQState;
    }

    public CPUTimeResult setSpentInIRQState(long spentInIRQState) {
        this.spentInIRQState = spentInIRQState;
        return this;
    }

    public long getSpentInSoftIRQState() {
        return spentInSoftIRQState;
    }

    public CPUTimeResult setSpentInSoftIRQState(long spentInSoftIRQState) {
        this.spentInSoftIRQState = spentInSoftIRQState;
        return this;
    }

    public long getSpentInStealState() {
        return spentInStealState;
    }

    public CPUTimeResult setSpentInStealState(long spentInStealState) {
        this.spentInStealState = spentInStealState;
        return this;
    }

    @Override
    public long getStatTime() {
        return statTime;
    }

    @Override public CPUTimeResult since(CPUTimeResult startResult) {
        return new CPUTimeResult(getStatTime())
                .setSpentInUserState(this.getSpentInUserState() - startResult.getSpentInUserState())
                .setSpentInNiceState(this.getSpentInNiceState() - startResult.getSpentInNiceState())
                .setSpentInSystemState(this.getSpentInSystemState() - startResult.getSpentInSystemState())
                .setSpentInIdleState(this.getSpentInIdleState() - startResult.getSpentInIdleState())
                .setSpentInIRQState(this.getSpentInIRQState() - startResult.getSpentInIRQState())
                .setSpentInSoftIRQState(this.getSpentInSoftIRQState() - startResult.getSpentInSoftIRQState())
                .setSpentInStealState(this.getSpentInStealState() - startResult.getSpentInStealState());
    }

    public double getCpuUsage() {
        return 1.0 - 1.0 * spentInIdleState / (
                this.spentInUserState
                        + this.spentInNiceState
                        + this.spentInSystemState
                        + this.spentInIdleState
                        + this.spentInIOWaitState
                        + this.spentInIRQState
                        + this.spentInSoftIRQState
                        + this.spentInStealState
        );
    }

    public String getCpuUsagePercent() {
        return new DecimalFormat("#.##").format(getCpuUsage() * 100);
    }

    @Override public JsonObject toJsonObject() {
        return new JsonObject()
                .put("stat_time", getStatTime())
                .put("User", this.spentInUserState)
                .put("Nice", this.spentInNiceState)
                .put("Idle", this.spentInIdleState)
                .put("IOWait", this.spentInIOWaitState)
                .put("IRQ", this.spentInIRQState)
                .put("SoftIRQ", this.spentInSoftIRQState)
                .put("Steal", this.spentInStealState)
                //.put("usage_raw", this.getCpuUsage())
                .put("usage", this.getCpuUsagePercent());
    }
}
