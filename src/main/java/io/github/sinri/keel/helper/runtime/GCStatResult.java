package io.github.sinri.keel.helper.runtime;

import io.vertx.core.json.JsonObject;

/**
 * @since 2.9.4
 */
public class GCStatResult implements RuntimeStatResult<GCStatResult> {
    private final long statTime;
    private long youngGCCount = 0;
    private long youngGCTime = 0;
    private long oldGCCount = 0;
    private long oldGCTime = 0;

    public GCStatResult() {
        this.statTime = System.currentTimeMillis();
    }

    private GCStatResult(long statTime) {
        this.statTime = statTime;
    }

    public long getYoungGCCount() {
        return youngGCCount;
    }

    @Deprecated
    public GCStatResult setYoungGCCount(long youngGCCount) {
        this.youngGCCount = youngGCCount;
        return this;
    }

    public GCStatResult addGCCountAsYoung(long youngGCCount) {
        this.youngGCCount += youngGCCount;
        return this;
    }

    public long getYoungGCTime() {
        return youngGCTime;
    }

    @Deprecated
    public GCStatResult setYoungGCTime(long youngGCTime) {
        this.youngGCTime = youngGCTime;
        return this;
    }

    public GCStatResult addGCTimeAsYoung(long youngGCTime) {
        this.youngGCTime += youngGCTime;
        return this;
    }

    public long getOldGCCount() {
        return oldGCCount;
    }

    @Deprecated
    public GCStatResult setOldGCCount(long oldGCCount) {
        this.oldGCCount = oldGCCount;
        return this;
    }

    public GCStatResult addGCCountAsOld(long oldGCCount) {
        this.oldGCCount += oldGCCount;
        return this;
    }

    public long getOldGCTime() {
        return oldGCTime;
    }

    @Deprecated
    public GCStatResult setOldGCTime(long oldGCTime) {
        this.oldGCTime = oldGCTime;
        return this;
    }

    public GCStatResult addGCTimeAsOld(long oldGCTime) {
        this.oldGCTime += oldGCTime;
        return this;
    }

    @Override public long getStatTime() {
        return statTime;
    }

    @Override
    public GCStatResult since(GCStatResult start) {
        return new GCStatResult(getStatTime())
                .addGCCountAsOld(getOldGCCount() - start.getOldGCCount())
                .addGCCountAsYoung(getYoungGCCount() - start.getYoungGCCount())
                .addGCTimeAsOld(getOldGCTime() - start.getOldGCTime())
                .addGCTimeAsYoung(getYoungGCTime() - start.getYoungGCTime())
                ;
    }

    @Override
    public JsonObject toJsonObject() {
        return new JsonObject()
                .put("stat_time", getStatTime())
                .put("major", new JsonObject()
                        .put("count", getOldGCCount())
                        .put("time", getOldGCTime())
                )
                .put("minor", new JsonObject()
                        .put("count", getYoungGCCount())
                        .put("time", getYoungGCTime())
                );
    }
}
