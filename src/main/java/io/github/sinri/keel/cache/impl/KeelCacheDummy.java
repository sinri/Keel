package io.github.sinri.keel.cache.impl;

import io.github.sinri.keel.cache.KeelCacheInterface;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.Map;

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
    public void save(@Nonnull K key, V value, long lifeInSeconds) {

    }

    @Override
    public V read(@Nonnull K key) {
        return null;
    }

    @Override
    public V read(@Nonnull K key, V fallbackValue) {
        return fallbackValue;
    }

    @Override
    public void remove(@Nonnull K key) {

    }

    @Override
    public void removeAll() {

    }

    @Override
    public void cleanUp() {

    }

    @Nonnull
    @Override
    public Map<K, V> getSnapshotMap() {
        return Collections.emptyMap();
    }
}
