package io.github.sinri.keel.helper.runtime;

import io.vertx.core.json.JsonObject;

import javax.annotation.Nonnull;
import java.lang.management.GarbageCollectorMXBean;
import java.util.HashSet;
import java.util.Set;

import static io.github.sinri.keel.facade.KeelInstance.Keel;
import static io.github.sinri.keel.helper.KeelHelpersInterface.KeelHelpers;

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

    public long getStatTime() {
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

    private static final Set<String> minorGCNames;
    private static final Set<String> majorGCNames;

    static {
        minorGCNames = new HashSet<>();
        majorGCNames = new HashSet<>();

        // Serial Collector： "Copy"（年轻代），"MarkSweepCompact"（老年代）
        minorGCNames.add("Copy");
        majorGCNames.add("MarkSweepCompact");
        //Parallel Collector： "PS Scavenge"（年轻代），"PS MarkSweep"（老年代）
        minorGCNames.add("PS Scavenge");
        majorGCNames.add("PS MarkSweep");
        // CMS (Concurrent Mark Sweep) Collector： "ParNew"（年轻代），"ConcurrentMarkSweep"（老年代）
        minorGCNames.add("ParNew");
        majorGCNames.add("ConcurrentMarkSweep");
        // G1 (Garbage-First) Collector： "G1 Young Generation"（年轻代），"G1 Old Generation"（老年代）
        minorGCNames.add("G1 Young Generation");
        majorGCNames.add("G1 Old Generation");
        // ZGC (Z Garbage Collector)： "ZGC"
        minorGCNames.add("ZGC");
        // Shenandoah： "Shenandoah Pauses"
        minorGCNames.add("Shenandoah Pauses");
    }

    /**
     * @since 3.1.4
     */
    public GCStatResult refreshWithGC(@Nonnull GarbageCollectorMXBean gc) {
        if (minorGCNames.contains(gc.getName())) {
            this.addGCCountAsYoung(gc.getCollectionCount());
            if (gc.getCollectionTime() >= 0) {
                this.addGCTimeAsYoung(gc.getCollectionTime());
            }
        } else if (majorGCNames.contains(gc.getName())) {
            this.addGCCountAsOld(gc.getCollectionCount());
            if (gc.getCollectionTime() >= 0) {
                this.addGCTimeAsOld(gc.getCollectionTime());
            }
        } else {
            Keel.getIssueRecorder().error(log -> log
                    .message("Found Unknown GarbageCollectorMXBean Name")
                    .context(new JsonObject()
                            .put("class", gc.getClass().getName())
                            .put("name", gc.getName())
                            .put("memoryPoolNames", KeelHelpers.stringHelper().joinStringArray(gc.getMemoryPoolNames(), ","))
                            .put("objectName", gc.getObjectName())
                            .put("collectionCount", gc.getCollectionCount())
                            .put("collectionTime", gc.getCollectionTime())
                    )
            );
        }
        return this;
    }
}
