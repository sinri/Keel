package io.github.sinri.keel.cache.impl;

import io.github.sinri.keel.cache.KeelCache;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * An implementation of KeelCacheInterface, using ConcurrentHashMap.
 *
 * @since 2.5
 */
public class KeelCacheImpl<K, V> implements KeelCache<K, V> {
    private final ConcurrentMap<K, ValueWrapper<V>> map;
    private long defaultLifeInSeconds = 1000L;

    public KeelCacheImpl() {
        this.map = new ConcurrentHashMap<>();
    }

    @Override
    public long getDefaultLifeInSeconds() {
        return defaultLifeInSeconds;
    }

    @Override
    public KeelCache<K, V> setDefaultLifeInSeconds(long lifeInSeconds) {
        defaultLifeInSeconds = lifeInSeconds;
        return this;
    }

    @Override
    public void save(@NotNull K key, V value, long lifeInSeconds) {
        this.map.put(key, new ValueWrapper<>(value, lifeInSeconds));
    }

    @Override
    public V read(@NotNull K key, V fallbackValue) {
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
    public void remove(@NotNull K key) {
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
    public synchronized @NotNull Map<K, V> getSnapshotMap() {
        Map<K, V> snapshot = new HashMap<>();
        this.map.keySet().forEach(key -> {
            ValueWrapper<V> vw = this.map.get(key);
            if (vw != null) {
                if (vw.isAliveNow()) {
                    snapshot.put(key, vw.getValue());
                }
            }
        });
        return Collections.unmodifiableMap(snapshot);
    }
}
