package io.github.sinri.keel.cache;

import io.github.sinri.keel.cache.caffeine.CaffeineCacheKit;

import java.util.function.Function;

/**
 * @param <K> class for key
 * @param <V> class for key
 * @since 1.9
 */
public interface KeelCacheInterface<K, V> {
    /**
     * @param <K> class for key
     * @param <V> class for key
     * @return an instance of CaffeineCacheKit since 1.9
     */
    static <K, V> KeelCacheInterface<K, V> createDefaultInstance() {
        return new CaffeineCacheKit<>();
    }

    void save(K key, V value, long lifeInSeconds);

    V read(K key);

    V read(K key, V fallbackValue);

    V read(K key, Function<? super K, ? extends V> fallbackValueGenerator, V fallbackValue);

    void remove(K key);

    void removeAll();

    /**
     * clean up the entries that is not alive (expired, etc.)
     */
    void cleanUp();
}
