package io.github.sinri.keel.cache.impl;

import io.github.sinri.keel.cache.KeelEverlastingCacheInterface;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @since 2.8.1
 */
public class KeelCacheVet<K, V> implements KeelEverlastingCacheInterface<K, V> {
    private final Lock lock;
    private Map<K, V> map;

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
        return map.getOrDefault(k, v);
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
            keys.forEach(key -> map.remove(key));
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

    @Override
    public void replaceAll(Map<K, V> newEntries) {
        lock.lock();
        try {
            map = new HashMap<>(newEntries);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public Map<K, V> getSnapshotMap() {
        return Collections.unmodifiableMap(map);
    }
}
