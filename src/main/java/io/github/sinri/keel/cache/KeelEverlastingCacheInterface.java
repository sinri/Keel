package io.github.sinri.keel.cache;

import io.github.sinri.keel.cache.impl.KeelCacheVet;

import java.util.Collection;
import java.util.Map;

/**
 * 
 * 
 * @since 2.9
 */
public interface KeelEverlastingCacheInterface<K, V> {
    static <K, V> KeelEverlastingCacheInterface<K, V> createDefaultInstance() {
        return new KeelCacheVet<>();
    }

    /**
     * Save the item to cache.
     */
    void save(K k, V v);

    void save(Map<K, V> appendEntries);

    /**
     *Returns cache value or null when not-existed.
 
     */
    default V read(K k) {
        return read(k, null);
    }

    /**
     *Returns cache value or default when not-existed.
 @param k key
     * @param v default value for the situation that key not existed
     *  
     */
    V read(K k, V v);

    /**
     * Remove the cached item with key.
     *
     * @param key key
     */
    void remove(K key);

    void remove(Collection<K> keys);

    /**
     * Remove all the cached items.
     */
    void removeAll();

    /**
     * Replace all entries in cache map with new entries.
     *
     * @param newEntries new map of entries
     */
    void replaceAll(Map<K, V> newEntries);

    /**
     * @return ConcurrentMap K → V alive value only
     * @since 1.14
     */
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
