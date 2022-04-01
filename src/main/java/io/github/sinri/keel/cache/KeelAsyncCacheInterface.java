package io.github.sinri.keel.cache;

import io.vertx.core.Future;

import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;

/**
 * @param <K>
 * @param <V>
 * @since 1.14
 */
public interface KeelAsyncCacheInterface<K, V> {

    /**
     * Save an item (as key and value pair) into cache, keep it available for a certain time.
     *
     * @param key           key
     * @param value         value
     * @param lifeInSeconds The lifetime of the cache item, in seconds.
     */
    Future<Void> save(K key, V value, long lifeInSeconds);

    /**
     * Read an available cached item with key, or return `null` when not found.
     *
     * @param key key
     * @return value of found available cached item, or `null`
     */
    Future<V> read(K key);

    /**
     * Read an available cached item with key, or return `fallbackValue` when not found.
     *
     * @param key           key
     * @param fallbackValue the certain value returned when not found
     * @return value of found available cached item, or `fallbackValue`
     */
    Future<V> read(K key, V fallbackValue);

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
    Future<V> read(K key, Function<? super K, ? extends V> fallbackValueGenerator, V fallbackValue);

    /**
     * Remove the cached item with key.
     *
     * @param key key
     */
    Future<Void> remove(K key);

    /**
     * Remove all the cached items.
     */
    Future<Void> removeAll();

    /**
     * clean up the entries that is not alive (expired, etc.)
     */
    Future<Void> cleanUp();

    /**
     * @return ConcurrentMap<K, V> alive value only
     * @since 1.14
     */
    Future<ConcurrentMap<K, V>> getSnapshotMap();
}
