package io.github.sinri.keel.core.helper;

import io.github.sinri.keel.Keel;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;

import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.util.Objects;

/**
 * @since 2.9.3
 */
public class KeelRuntimeHelper {
    private static final KeelRuntimeHelper instance = new KeelRuntimeHelper();

    private KeelRuntimeHelper() {

    }

    static KeelRuntimeHelper getInstance() {
        return instance;
    }

    public void monitorGC(long interval, Handler<GCStat> handler) {
        Keel.getVertx().setPeriodic(interval, timer -> handler.handle(getGCInfo()));
    }

    public GCStat getGCInfo() {
        GCStat gcStat = new GCStat();
        for (GarbageCollectorMXBean gc : ManagementFactory.getGarbageCollectorMXBeans()) {
            if (Objects.equals("G1 Young Generation", gc.getName())) {
                gcStat.addYoungGCCount(gc.getCollectionCount());
                if (gc.getCollectionTime() >= 0) {
                    gcStat.addYoungGCTime(gc.getCollectionTime());
                }
            } else if (Objects.equals("G1 Old Generation", gc.getName())) {
                gcStat.addOldGCCount(gc.getCollectionCount());
                if (gc.getCollectionTime() >= 0) {
                    gcStat.addOldGCTime(gc.getCollectionTime());
                }
            } else {
                Keel.outputLogger().warning("Found Unknown GarbageCollectorMXBean Name", new JsonObject()
                        .put("class", gc.getClass().getName())
                        .put("name", gc.getName())
                        .put("memoryPoolNames", Keel.helpers().string().joinStringArray(gc.getMemoryPoolNames(), ","))
                        .put("objectName", gc.getObjectName())
                        .put("collectionCount", gc.getCollectionCount())
                        .put("collectionTime", gc.getCollectionTime())
                );
            }
        }
        return gcStat;
    }

    public static class GCStat {
        private final long statTime;
        private long youngGCCount = 0;
        private long youngGCTime = 0;
        private long oldGCCount = 0;
        private long oldGCTime = 0;

        public GCStat() {
            this.statTime = System.currentTimeMillis();
        }

        public long getYoungGCCount() {
            return youngGCCount;
        }

        public GCStat setYoungGCCount(long youngGCCount) {
            this.youngGCCount = youngGCCount;
            return this;
        }

        public GCStat addYoungGCCount(long youngGCCount) {
            this.youngGCCount += youngGCCount;
            return this;
        }

        public long getYoungGCTime() {
            return youngGCTime;
        }

        public GCStat setYoungGCTime(long youngGCTime) {
            this.youngGCTime = youngGCTime;
            return this;
        }

        public GCStat addYoungGCTime(long youngGCTime) {
            this.youngGCTime += youngGCTime;
            return this;
        }

        public long getOldGCCount() {
            return oldGCCount;
        }

        public GCStat setOldGCCount(long oldGCCount) {
            this.oldGCCount = oldGCCount;
            return this;
        }

        public GCStat addOldGCCount(long oldGCCount) {
            this.oldGCCount += oldGCCount;
            return this;
        }

        public long getOldGCTime() {
            return oldGCTime;
        }

        public GCStat setOldGCTime(long oldGCTime) {
            this.oldGCTime = oldGCTime;
            return this;
        }

        public GCStat addOldGCTime(long oldGCTime) {
            this.oldGCTime += oldGCTime;
            return this;
        }

        public long getStatTime() {
            return statTime;
        }
    }
}
