package io.github.sinri.keel.cache;

import io.github.sinri.keel.cache.impl.KeelCacheBet;
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
     * @since 2.5
     */
    static <K, V> KeelAsyncCacheInterface<K, V> createDefaultInstance() {
        return new KeelCacheBet<>();
    }

    /**
     * Save an item (as key and value pair) into cache, keep it available for a certain time.
     *
     * @param key           key
     * @param value         value
     * @param lifeInSeconds The lifetime of the cache item, in seconds.
     */
    Future<Void> save(K key, V value, long lifeInSeconds);

    /**
     * Read an available cached item with key in returned future,
     * or return a failed future of NotCached.
     *
     * @param key key
     * @return value of found available cached item, or `null`
     */
    Future<V> read(K key);

    /**
     * Read an available cached item with key, or return `fallbackValue` when not found;
     * no failed future.
     *
     * @param key           key
     * @param fallbackValue the certain value returned when not found
     * @return value of found available cached item, or `fallbackValue`
     */
    Future<V> read(K key, V fallbackValue);

    /**
     * Read an available cached item with key;
     * if not found, try to generate one with key using `fallbackValueGenerator` to save into cache, then return it in the future;
     * if failed to generate, failed future instead.
     *
     * @param key           key
     * @param generator     function to generate a value for given key, to be saved into cache and return when no cached item found
     * @param lifeInSeconds cache available in this period, in seconds
     * @return the valued read from cache
     * @since 2.5
     */
    Future<V> read(K key, Function<K, Future<V>> generator, long lifeInSeconds);

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
     * @return ConcurrentMap K → V alive value only
     * @since 1.14
     */
    Future<ConcurrentMap<K, V>> getSnapshotMap();

    class NotCached extends Exception {
        public NotCached(String key) {
            super("For key [" + key + "], no available cached record found.");
        }
    }
}
