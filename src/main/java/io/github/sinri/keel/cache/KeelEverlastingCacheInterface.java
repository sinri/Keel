package io.github.sinri.keel.cache;

import io.github.sinri.keel.cache.impl.KeelCacheVet;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Map;

/**
 * @param <K>
 * @param <V>
 * @since 2.9
 */
public interface KeelEverlastingCacheInterface<K, V> {
    static <K, V> KeelEverlastingCacheInterface<K, V> createDefaultInstance() {
        return new KeelCacheVet<>();
    }

    /**
     * Save the item to cache.
     */
    void save(@Nonnull K k, V v);

    void save(@Nonnull Map<K, V> appendEntries);

    /**
     * @return cache value or null when not-existed
     */
    default V read(@Nonnull K k) {
        return read(k, null);
    }

    /**
     * @param k key
     * @param v default value for the situation that key not existed
     * @return @return cache value or default when not-existed
     */
    V read(@Nonnull K k, V v);

    /**
     * Remove the cached item with key.
     *
     * @param key key
     */
    void remove(@Nonnull K key);

    void remove(@Nonnull Collection<K> keys);

    /**
     * Remove all the cached items.
     */
    void removeAll();

    /**
     * Replace all entries in cache map with new entries.
     *
     * @param newEntries new map of entries
     */
    void replaceAll(@Nonnull Map<K, V> newEntries);

    /**
     * @return ConcurrentMap K → V alive value only
     * @since 1.14
     */
    @Nonnull
    Map<K, V> getSnapshotMap();

//    class LockedException extends Exception{
//        public LockedException(){
//            super("KeelEverlastingCacheInterface Locked");
//        }
//        public LockedException(String msg){
//            super(msg);
//        }
//        public LockedException(Throwable throwable){
//            super(throwable);
//        }
//    }
}
