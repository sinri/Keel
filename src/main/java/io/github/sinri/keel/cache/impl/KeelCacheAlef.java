package io.github.sinri.keel.cache.impl;

import io.github.sinri.keel.cache.KeelCacheInterface;
import io.github.sinri.keel.cache.ValueWrapper;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * An implementation of KeelCacheInterface, using ConcurrentHashMap.
 *
 * @since 2.5
 */
public class KeelCacheAlef<K, V> implements KeelCacheInterface<K, V> {
    private final ConcurrentMap<K, ValueWrapper<V>> map;
    private long defaultLifeInSeconds = 1000L;

    public KeelCacheAlef() {
        this.map = new ConcurrentHashMap<>();
    }

    @Override
    public long getDefaultLifeInSeconds() {
        return defaultLifeInSeconds;
    }

    @Override
    public KeelCacheInterface<K, V> setDefaultLifeInSeconds(long lifeInSeconds) {
        defaultLifeInSeconds = lifeInSeconds;
        return this;
    }

    @Override
    public void save(K key, V value, long lifeInSeconds) {
        this.map.put(key, new ValueWrapper<>(value, lifeInSeconds));
    }

    @Override
    public V read(K key, V fallbackValue) {
        ValueWrapper<V> vw = this.map.get(key);
        if (vw == null) {
            return fallbackValue;
        }
        if (vw.isAliveNow()) {
            return vw.getValue();
        } else {
            return fallbackValue;
        }
    }

    @Override
    public void remove(K key) {
        this.map.remove(key);
    }

    @Override
    public void removeAll() {
        this.map.clear();
    }

    @Override
    public void cleanUp() {
        this.map.keySet().forEach(key -> {
            ValueWrapper<V> vw = this.map.get(key);
            if (vw != null) {
                if (!vw.isAliveNow()) {
                    this.map.remove(key, vw);
                }
            }
        });
    }

    @Override
    public ConcurrentMap<K, V> getSnapshotMap() {
        ConcurrentMap<K, V> snapshot = new ConcurrentHashMap<>();
        this.map.keySet().forEach(key -> {
            ValueWrapper<V> vw = this.map.get(key);
            if (vw != null) {
                if (!vw.isAliveNow()) {
                    snapshot.put(key, vw.getValue());
                }
            }
        });
        return snapshot;
    }
}
