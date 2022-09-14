package io.github.sinri.keel.cache.impl;

import io.github.sinri.keel.cache.KeelCacheInterface;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class KeelCacheDummy<K, V> implements KeelCacheInterface<K, V> {

    @Override
    public long getDefaultLifeInSeconds() {
        return 0;
    }

    @Override
    public KeelCacheInterface<K, V> setDefaultLifeInSeconds(long lifeInSeconds) {
        return this;
    }

    @Override
    public void save(K key, V value, long lifeInSeconds) {

    }

    @Override
    public V read(K key) {
        return null;
    }

    @Override
    public V read(K key, V fallbackValue) {
        return fallbackValue;
    }

    @Override
    public void remove(K key) {

    }

    @Override
    public void removeAll() {

    }

    @Override
    public void cleanUp() {

    }

    @Override
    public ConcurrentMap<K, V> getSnapshotMap() {
        return new ConcurrentHashMap<>();
    }
}
