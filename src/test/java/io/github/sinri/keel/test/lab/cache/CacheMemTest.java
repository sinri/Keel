package io.github.sinri.keel.test.lab.cache;

import io.github.sinri.keel.cache.KeelCacheInterface;
import io.github.sinri.keel.helper.runtime.KeelRuntimeMonitor;
import io.github.sinri.keel.tesuto.KeelTest;
import io.github.sinri.keel.tesuto.TestUnit;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
import org.jetbrains.annotations.NotNull;

import static io.github.sinri.keel.facade.KeelInstance.Keel;

public class CacheMemTest extends KeelTest {
    @NotNull
    @Override
    protected Future<Void> starting() {
        new KeelRuntimeMonitor().startRuntimeMonitor(5_000L, ms -> {
            getLogger().info("Monitor |" +
                    " HEAP=" + ms.getJvmMemoryResult().getMxHeapUsedBytes()
                    +
                    " YGC=" + ms.getGCStat().getYoungGCCount()
                    +
                    " FGC=" + ms.getGCStat().getOldGCCount()
            );
        });
        return super.starting();
    }

    @TestUnit
    public Future<Void> test1() {
        KeelCacheInterface<String, JsonObject> cache = KeelCacheInterface.createDefaultInstance();

        Keel.getVertx().setPeriodic(1000L, t -> {
            for (long i = 0; i < 1000; i++) {
                cache.save("key-" + i + "-" + System.currentTimeMillis(), new JsonObject().put("value", i), 1);
            }
        });

        Keel.getVertx().setPeriodic(3000L, 3000L, t -> {
            cache.cleanUp();
        });

        Promise<Void> promise = Promise.promise();

        Keel.getVertx().setTimer(1000_000L, t -> {
            promise.complete();
        });

        return promise.future();
    }
}
