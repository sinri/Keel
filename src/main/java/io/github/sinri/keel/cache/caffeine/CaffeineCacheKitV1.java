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
//        this.cache = Caffeine.newBuilder()
//                //cache的初始容量
//                .initialCapacity(2)
//                //cache最大缓存数
//                .maximumSize(4)
//                //设置写缓存后n秒钟过期
//                .expireAfterWrite(10, TimeUnit.SECONDS)
//                //设置读写缓存后n秒钟过期,实际很少用到,类似于expireAfterWrite
//                //.expireAfterAccess(17, TimeUnit.SECONDS)
//                .build();
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
