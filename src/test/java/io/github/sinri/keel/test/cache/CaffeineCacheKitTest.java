package io.github.sinri.keel.test.cache;

import io.github.sinri.keel.cache.KeelCacheInterface;

import java.util.Date;

public class CaffeineCacheKitTest {
    public static void main(String[] args) throws InterruptedException {
        KeelCacheInterface<String, Long> cache = KeelCacheInterface.createDefaultInstance();
        long time_saved = new Date().getTime();
        cache.save("a", time_saved, 3);
        for (int i = 0; i < 6; i++) {
            Long a = cache.read("a");
            System.out.println("i=" + i + " read as " + a + " now=" + (new Date().getTime()));
            cache.getSnapshotMap().forEach((k, v) -> {
                System.out.println("> k=" + k + " v=" + v);
            });
            cache.cleanUp();
            Thread.sleep(1000);
        }

    }
}
