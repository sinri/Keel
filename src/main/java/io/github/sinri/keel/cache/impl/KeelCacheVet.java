package io.github.sinri.keel.cache.impl;

import io.github.sinri.keel.cache.KeelEverlastingCacheInterface;

import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @since 2.9
 */
public class KeelCacheVet<K, V> implements KeelEverlastingCacheInterface<K, V> {
    private final Lock lock;
    private final Map<K, V> map;

    public KeelCacheVet() {
        lock = new ReentrantLock();
        map = new HashMap<>();
    }


    @Override
    public void save(K k, V v) {
        lock.lock();
        try {
            map.put(k, v);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void save(Map<K, V> appendEntries) {
        lock.lock();
        try {
            map.putAll(appendEntries);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public V read(K k, V v) {
        lock.lock();try {
            
        V r;
        r = map.getOrDefault(k, v);
        } finally {
            lock.unlock();
        }
        return r;
    }

    @Override
    public void remove(K key) {
        lock.lock();
        try {
            map.remove(key);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void remove(Collection<K> keys) {
        lock.lock();
        try {
            keys.forEach(map::remove);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void removeAll() {
        lock.lock();
        try {
            map.clear();
        } finally {
            lock.unlock();
        }
    }

    /**
     * @param newEntries new map of entries
     * @since 2.9.4 no longer implemented by replace map
     */
    @Override
    public void replaceAll(Map<K, V> newEntries) {
        lock.lock();
        try {
            Set<K> ks = newEntries.keySet();
            map.putAll(newEntries);
            map.keySet().forEach(k -> {
                if (!ks.contains(k)) {
                    map.remove(k);
                }
            });
        } finally {
            lock.unlock();
        }
    }

    @Override
    public Map<K, V> getSnapshotMap() {
        return Collections.unmodifiableMap(map);
    }
}
