package io.github.sinri.keel.cache.impl;

import io.github.sinri.keel.cache.KeelCache;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Map;

public class KeelCacheDummy<K, V> implements KeelCache<K, V> {

    @Override
    public long getDefaultLifeInSeconds() {
        return 0;
    }

    @Override
    public KeelCache<K, V> setDefaultLifeInSeconds(long lifeInSeconds) {
        return this;
    }

    @Override
    public void save(@NotNull K key, V value, long lifeInSeconds) {

    }

    @Override
    public V read(@NotNull K key) {
        return null;
    }

    @Override
    public V read(@NotNull K key, V fallbackValue) {
        return fallbackValue;
    }

    @Override
    public void remove(@NotNull K key) {

    }

    @Override
    public void removeAll() {

    }

    @Override
    public void cleanUp() {

    }

    @Override
    public @NotNull Map<K, V> getSnapshotMap() {
        return Collections.emptyMap();
    }
}
