package io.github.sinri.keel.cache;

import io.github.sinri.keel.cache.impl.KeelCacheGimel;
import io.vertx.core.Future;

import javax.annotation.Nonnull;
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
    Future<Void> save(@Nonnull K k, V v);

    Future<Void> save(@Nonnull Map<K, V> appendEntries);

    /**
     * @return cache value or null when not-existed
     * @since 2.9.4 return Future
     */
    default Future<V> read(@Nonnull K k) {
        return read(k, null);
    }

    /**
     * @param k key
     * @param v default value for the situation that key not existed
     * @return @return cache value or default when not-existed
     * @since 2.9.4 return Future
     */
    Future<V> read(@Nonnull K k, V v);

    /**
     * Remove the cached item with key.
     *
     * @param key key
     */
    Future<Void> remove(@Nonnull K key);

    Future<Void> remove(@Nonnull Collection<K> keys);

    /**
     * Remove all the cached items.
     */
    Future<Void> removeAll();

    /**
     * Replace all entries in cache map with new entries.
     *
     * @param newEntries new map of entries
     */
    Future<Void> replaceAll(@Nonnull Map<K, V> newEntries);

    /**
     * @return ConcurrentMap K → V alive value only
     * @since 1.14
     */
    @Nonnull
    Map<K, V> getSnapshotMap();
}
