package io.github.sinri.keel.cache;

import io.github.sinri.keel.cache.impl.KeelEverlastingCacheImpl;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Map;

/**
 * @param <K>
 * @param <V>
 * @since 2.9
 */
public interface KeelEverlastingCache<K, V> {
    static <K, V> KeelEverlastingCache<K, V> create() {
        return new KeelEverlastingCacheImpl<>();
    }

    /**
     * Save the item to cache.
     */
    void save(@NotNull K k, V v);

    void save(@NotNull Map<K, V> appendEntries);

    /**
     * @return cache value or null when not-existed
     */
    default V read(@NotNull K k) {
        return read(k, null);
    }

    /**
     * @param k key
     * @param v default value for the situation that key not existed
     * @return @return cache value or default when not-existed
     */
    V read(@NotNull K k, V v);

    /**
     * Remove the cached item with key.
     *
     * @param key key
     */
    void remove(@NotNull K key);

    void remove(@NotNull Collection<K> keys);

    /**
     * Remove all the cached items.
     */
    void removeAll();

    /**
     * Replace all entries in cache map with new entries.
     *
     * @param newEntries new map of entries
     */
    void replaceAll(@NotNull Map<K, V> newEntries);

    /**
     * @return ConcurrentMap K → V alive value only
     * @since 1.14
     */
    @NotNull
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
