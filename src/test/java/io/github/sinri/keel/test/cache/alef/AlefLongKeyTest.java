package io.github.sinri.keel.test.cache.alef;

import io.github.sinri.keel.Keel;
import io.github.sinri.keel.cache.KeelCacheInterface;
import io.github.sinri.keel.test.SharedTestBootstrap;

public class AlefLongKeyTest {
    public static void main(String[] args) {
        SharedTestBootstrap.initialize(v0 -> {
            KeelCacheInterface<String, String> cache = KeelCacheInterface.createDefaultInstance();

            String key1 = "com.leqee.oc.tachiba.catholic.service.v1.QueryUserListService||[\"jfan-wbkf\"]|||[\"NORMAL\"]";
            String key2 = "com.leqee.oc.tachiba.catholic.service.v1.QueryUserListService||[\"jfan2\"]|||[\"NORMAL\"]";

            cache.save(key1, "1");
            cache.save(key2, "2");
            Keel.outputLogger().info("key1 -> " + cache.read(key1));
            Keel.outputLogger().info("key2 -> " + cache.read(key2));

        });
    }
}
