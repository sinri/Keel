package io.github.sinri.keel.cache.impl;

import io.github.sinri.keel.cache.KeelEverlastingCacheInterface;
import org.jetbrains.annotations.NotNull;

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
    public void save(@NotNull K k, V v) {
        lock.lock();
        try {
            map.put(k, v);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void save(@NotNull Map<K, V> appendEntries) {
        lock.lock();
        try {
            map.putAll(appendEntries);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public V read(@NotNull K k, V v) {
        V r;
        lock.lock();
        try {
            r = map.getOrDefault(k, v);
        } finally {
            lock.unlock();
        }
        return r;
    }

    @Override
    public void remove(@NotNull K key) {
        lock.lock();
        try {
            map.remove(key);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void remove(@NotNull Collection<K> keys) {
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
    public void replaceAll(@NotNull Map<K, V> newEntries) {
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

    public @NotNull Map<K, V> getSnapshotMap() {
        return Collections.unmodifiableMap(map);
    }
}
