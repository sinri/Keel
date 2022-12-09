package io.github.sinri.keel.cache;

import io.github.sinri.keel.cache.impl.KeelCacheGimel;
import io.vertx.core.Future;

import java.util.Collection;
import java.util.Map;

/**
 * @param <K>
 * @param <V>
 * @since 2.9
 */
public interface KeelAsyncEverlastingCacheInterface<K, V> {
    static <K, V> KeelAsyncEverlastingCacheInterface<K, V> createDefaultInstance() {
        return new KeelCacheGimel<>();
    }

    default long getLockWaitMs() {
        return 100;
    }

    /**
     * Save the item to cache.
     */
    Future<Void> save(K k, V v);

    Future<Void> save(Map<K, V> appendEntries);

    /**
     * @return cache value or null when not-existed
     * @since 2.9.4 return Future<V>
     */
    default Future<V> read(K k) {
        return read(k, null);
    }

    /**
     * @param k key
     * @param v default value for the situation that key not existed
     * @return @return cache value or default when not-existed
     * @since 2.9.4 return Future<V>
     */
    Future<V> read(K k, V v);

    /**
     * Remove the cached item with key.
     *
     * @param key key
     */
    Future<Void> remove(K key);

    Future<Void> remove(Collection<K> keys);

    /**
     * Remove all the cached items.
     */
    Future<Void> removeAll();

    /**
     * Replace all entries in cache map with new entries.
     *
     * @param newEntries new map of entries
     */
    Future<Void> replaceAll(Map<K, V> newEntries);

    /**
     * @return ConcurrentMap K → V alive value only
     * @since 1.14
     */
    Map<K, V> getSnapshotMap();
}
