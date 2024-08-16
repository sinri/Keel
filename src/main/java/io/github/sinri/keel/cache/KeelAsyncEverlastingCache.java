package io.github.sinri.keel.cache;

import io.github.sinri.keel.cache.impl.KeelAsyncEverlastingCacheImpl;
import io.vertx.core.Future;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Map;

/**
 * @param <K>
 * @param <V>
 * @since 2.9
 */
public interface KeelAsyncEverlastingCache<K, V> {
    static <K, V> KeelAsyncEverlastingCache<K, V> create() {
        return new KeelAsyncEverlastingCacheImpl<>();
    }

    default long getLockWaitMs() {
        return 100;
    }

    /**
     * Save the item to cache.
     */
    Future<Void> save(@NotNull K k, V v);

    Future<Void> save(@NotNull Map<K, V> appendEntries);

    /**
     * @return cache value or null when not-existed
     * @since 2.9.4 return Future
     */
    default Future<V> read(@NotNull K k) {
        return read(k, null);
    }

    /**
     * @param k key
     * @param v default value for the situation that key not existed
     * @return @return cache value or default when not-existed
     * @since 2.9.4 return Future
     */
    Future<V> read(@NotNull K k, V v);

    /**
     * Remove the cached item with key.
     *
     * @param key key
     */
    Future<Void> remove(@NotNull K key);

    Future<Void> remove(@NotNull Collection<K> keys);

    /**
     * Remove all the cached items.
     */
    Future<Void> removeAll();

    /**
     * Replace all entries in cache map with new entries.
     *
     * @param newEntries new map of entries
     */
    Future<Void> replaceAll(@NotNull Map<K, V> newEntries);

    /**
     * @return ConcurrentMap K → V alive value only
     * @since 1.14
     */
    @NotNull
    Map<K, V> getSnapshotMap();
}
