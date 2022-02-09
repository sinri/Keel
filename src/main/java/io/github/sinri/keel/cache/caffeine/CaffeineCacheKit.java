package io.github.sinri.keel.cache.caffeine;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Expiry;
import io.github.sinri.keel.cache.KeelCacheInterface;
import org.checkerframework.checker.index.qual.NonNegative;

import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * @param <K> class for key
 * @param <V> class for value
 * @since 1.9
 */
public class CaffeineCacheKit<K, V> implements KeelCacheInterface<K, V> {
    protected Cache<K, ValueWrapper<V>> cache;

    public CaffeineCacheKit() {
        this.cache = Caffeine.newBuilder()
                .expireAfter(new Expiry<K, ValueWrapper<V>>() {
                    @Override
                    public long expireAfterCreate(K key, ValueWrapper<V> valueWrapper, long currentTime) {
                        return TimeUnit.SECONDS.toNanos(valueWrapper.getDeath());
                    }

                    @Override
                    public long expireAfterUpdate(K key, ValueWrapper<V> valueWrapper, long currentTime, @NonNegative long currentDuration) {
                        return TimeUnit.SECONDS.toNanos(valueWrapper.getDeath());
                    }

                    @Override
                    public long expireAfterRead(K key, ValueWrapper<V> valueWrapper, long currentTime, @NonNegative long currentDuration) {
                        return TimeUnit.SECONDS.toNanos(valueWrapper.getDeath());
                    }
                })
                .build();
    }

    public void save(K key, V value, long lifeInSeconds) {
        this.cache.put(key, new ValueWrapper<>(value, lifeInSeconds));
    }

    public V read(K key) {
        return this.read(key, null);
    }

    public V read(K key, V fallbackValue) {
        ValueWrapper<V> valueWrapper = this.cache.getIfPresent(key);
        if (valueWrapper == null) return fallbackValue;
        return valueWrapper.getValue();
    }

    public V read(K key, Function<? super K, ? extends V> fallbackValueGenerator, V fallbackValue) {
        ValueWrapper<V> valueWrapper = this.cache.get(
                key,
                k -> {
                    V v = fallbackValueGenerator.apply(k);
                    return new ValueWrapper<>(v, 1);
                }
        );
        if (valueWrapper == null) return fallbackValue;
        return valueWrapper.getValue();
    }

    public void remove(K key) {
        this.cache.invalidate(key);
    }

    public void removeAll() {
        this.cache.invalidateAll();
    }

    public void cleanUp() {
        this.cache.cleanUp();
    }

    protected static class ValueWrapper<P> {
        private final P value;
        private final long death;
        private final long birth;

        public ValueWrapper(P value, long lifeInSeconds) {
            this.value = value;
            this.birth = new Date().getTime();
            this.death = this.birth + lifeInSeconds * 1000L;
        }

        public long getBirth() {
            return birth;
        }

        public long getDeath() {
            return death;
        }

        public P getValue() {
            return value;
        }
    }
}
