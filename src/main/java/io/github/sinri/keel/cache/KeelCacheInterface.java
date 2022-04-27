package io.github.sinri.keel.cache;

import io.github.sinri.keel.cache.caffeine.CaffeineCacheKit;
import io.vertx.core.Future;

import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;

/**
 * @param <K> class for key
 * @param <V> class for key
 * @since 1.9
 */
public interface KeelCacheInterface<K, V> {
    /**
     * @param <K> class for key
     * @param <V> class for value
     * @return A new instance of KeelCacheInterface created.
     * @since 1.9 Use CaffeineCacheKit as implementation by default.
     */
    static <K, V> KeelCacheInterface<K, V> createDefaultInstance() {
        return new CaffeineCacheKit<>();
    }

    /**
     * Save an item (as key and value pair) into cache, keep it available for a certain time.
     *
     * @param key           key
     * @param value         value
     * @param lifeInSeconds The lifetime of the cache item, in seconds.
     */
    void save(K key, V value, long lifeInSeconds);

    /**
     * Read an available cached item with key, or return `null` when not found.
     *
     * @param key key
     * @return value of found available cached item, or `null`
     */
    V read(K key);

    /**
     * Read an available cached item with key, or return `fallbackValue` when not found.
     *
     * @param key           key
     * @param fallbackValue the certain value returned when not found
     * @return value of found available cached item, or `fallbackValue`
     */
    V read(K key, V fallbackValue);

    /**
     * Read an available cached item with key;
     * if not found, try to generate one with key using `fallbackValueGenerator` to return;
     * if still gets `null`, return `fallbackValue`.
     *
     * @param key                    key
     * @param fallbackValueGenerator fallback value generator, a function receive key and return value
     * @param fallbackValue          the certain value returned when not found and generator returned `null`
     * @return value of found available cached item, or generated one, or `fallbackValue`
     */
    V read(K key, Function<? super K, ? extends V> fallbackValueGenerator, V fallbackValue);

    /**
     * Read an available cached item with key;
     * if not found, try to generate one with key using `fallbackValueGenerator` to return;
     * if still gets `null`, return `fallbackValue`.
     *
     * @param key                    key
     * @param fallbackValueGenerator fallback value generator, a function receive key and return value
     * @param fallbackValue          the certain value returned when not found and generator returned `null`
     * @param lifeInSeconds          life in seconds of the newly created
     * @return value of found available cached item, or generated one, or `fallbackValue`
     * @since 1.14
     */
    V read(K key, Function<? super K, ? extends V> fallbackValueGenerator, V fallbackValue, long lifeInSeconds);

    /**
     * Remove the cached item with key.
     *
     * @param key key
     */
    void remove(K key);

    /**
     * Remove all the cached items.
     */
    void removeAll();

    /**
     * clean up the entries that is not alive (expired, etc.)
     */
    void cleanUp();

    /**
     * @return ConcurrentMap K → V alive value only
     * @since 1.14
     */
    ConcurrentMap<K, V> getSnapshotMap();

    /**
     * @param cache         KeelCacheInterface
     * @param key           cache key
     * @param generator     if not cached for key, use this function to generate one asynchronously
     * @param lifeInSeconds life in seconds
     * @param <K>           key type
     * @param <V>           value type
     * @return future of value
     * @since 2.1
     */
    static <K, V> Future<V> ensureAndRead(KeelCacheInterface<K, V> cache, K key, Function<K, Future<V>> generator, long lifeInSeconds) {
        V existed = cache.read(key);
        if (existed != null) {
            return Future.succeededFuture(existed);
        }
        return generator.apply(key)
                .compose(v -> {
                    cache.save(key, v, lifeInSeconds);
                    return Future.succeededFuture(v);
                });
    }
}
