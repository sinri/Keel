package io.github.sinri.keel.cache.caffeine;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import java.util.concurrent.TimeUnit;

@Deprecated
public class CaffeineCacheKitV1<T> {
    protected Cache<String, T> cache;

    public CaffeineCacheKitV1(Cache<String, T> cache) {
        this.cache = cache;
        //创建guava cache
    }

    public static <K> CaffeineCacheKitV1<K> makeAnExpireAfterWrite(int seconds) {
        return new CaffeineCacheKitV1<>(
                Caffeine.newBuilder()
                        .expireAfterWrite(seconds, TimeUnit.SECONDS)
                        .build()
        );
    }

    public void save(String key, T value) {
        this.cache.put(key, value);
    }

    public T read(String key) {
        return this.cache.getIfPresent(key);
    }

}
