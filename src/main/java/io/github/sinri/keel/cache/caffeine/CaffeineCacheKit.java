package io.github.sinri.keel.cache.caffeine;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Expiry;
import io.github.sinri.keel.cache.KeelCacheInterface;
import io.github.sinri.keel.cache.ValueWrapper;
import org.checkerframework.checker.index.qual.NonNegative;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

/**
 * @param <K> class for key
 * @param <V> class for value
 * @since 1.9
 */
public class CaffeineCacheKit<K, V> implements KeelCacheInterface<K, V> {
    protected final Cache<K, ValueWrapper<V>> cache;

    public CaffeineCacheKit() {
        this.cache = Caffeine.newBuilder()
                .expireAfter(new Expiry<K, ValueWrapper<V>>() {
                    @Override
                    public long expireAfterCreate(K key, ValueWrapper<V> valueWrapper, long currentTime) {
                        return TimeUnit.SECONDS.toNanos(valueWrapper.getDeath()) - currentTime;
                    }

                    @Override
                    public long expireAfterUpdate(K key, ValueWrapper<V> valueWrapper, long currentTime, @NonNegative long currentDuration) {
                        return TimeUnit.SECONDS.toNanos(valueWrapper.getDeath()) - currentTime;
                    }

                    @Override
                    public long expireAfterRead(K key, ValueWrapper<V> valueWrapper, long currentTime, @NonNegative long currentDuration) {
                        return TimeUnit.SECONDS.toNanos(valueWrapper.getDeath()) - currentTime;
                    }
                })
                .build();
    }

    @Override
    public void save(K key, V value, long lifeInSeconds) {
        this.cache.put(key, new ValueWrapper<>(value, lifeInSeconds));
    }

    @Override
    public V read(K key) {
        return this.read(key, null);
    }

    @Override
    public V read(K key, V fallbackValue) {
        ValueWrapper<V> valueWrapper = this.cache.getIfPresent(key);
        if (valueWrapper == null) return fallbackValue;
        if (valueWrapper.isAliveNow()) {
            return valueWrapper.getValue();
        } else {
            return fallbackValue;
        }
    }

    @Override
    public void remove(K key) {
        this.cache.invalidate(key);
    }

    @Override
    public void removeAll() {
        this.cache.invalidateAll();
    }

    @Override
    public void cleanUp() {
        this.cache.cleanUp();
    }

    /**
     * @return ConcurrentMap, K → V
     * Note: Modify the returned map would not affect the raw cache
     * @since 1.14
     */
    @Override
    public ConcurrentMap<K, V> getSnapshotMap() {
        ConcurrentMap<K, V> map = new ConcurrentHashMap<>();
        this.cache.asMap().forEach((k, v) -> {
            if (v.isAliveNow()) {
                map.put(k, v.getValue());
            }
        });
        return map;
    }
}
