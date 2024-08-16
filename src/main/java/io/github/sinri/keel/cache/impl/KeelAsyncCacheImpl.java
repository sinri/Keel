package io.github.sinri.keel.cache.impl;

import io.github.sinri.keel.cache.KeelAsyncCache;
import io.vertx.core.Future;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;

public class KeelAsyncCacheImpl<K, V> implements KeelAsyncCache<K, V> {
    private final ConcurrentMap<K, ValueWrapper<V>> map;

    public KeelAsyncCacheImpl() {
        this.map = new ConcurrentHashMap<>();
    }

    @Override
    public Future<Void> save(@NotNull K key, V value, long lifeInSeconds) {
        this.map.put(key, new ValueWrapper<>(value, lifeInSeconds));
        return Future.succeededFuture();
    }

    @Override
    public Future<V> read(@NotNull K key) {
        ValueWrapper<V> vw = this.map.get(key);
        if (vw == null || !vw.isAliveNow()) {
            return Future.failedFuture(new NotCached(key.toString()));
        }
        return Future.succeededFuture(vw.getValue());
    }

    @Override
    public Future<V> read(@NotNull K key, V fallbackValue) {
        ValueWrapper<V> vw = this.map.get(key);
        if (vw == null) {
            return Future.succeededFuture(fallbackValue);
        } else {
            if (vw.isAliveNow()) {
                return Future.succeededFuture(vw.getValue());
            } else {
                return Future.succeededFuture(fallbackValue);
            }
        }
    }

    @Override
    public Future<V> read(@NotNull K key, Function<K, Future<V>> generator, long lifeInSeconds) {
        // i.e. computeIfAbsent
        ValueWrapper<V> vw = this.map.get(key);
        if (vw != null && vw.isAliveNow()) {
            return Future.succeededFuture(vw.getValue());
        } else {
            return generator.apply(key)
                    .compose(v -> {
                        return save(key, v, lifeInSeconds)
                                .compose(saved -> {
                                    return Future.succeededFuture(v);
                                });
                    });

        }
    }

    @Override
    public Future<Void> remove(@NotNull K key) {
        this.map.remove(key);
        return Future.succeededFuture();
    }

    @Override
    public Future<Void> removeAll() {
        this.map.clear();
        return Future.succeededFuture();
    }

    @Override
    public Future<Void> cleanUp() {
        this.map.keySet().forEach(key -> {
            ValueWrapper<V> vw = this.map.get(key);
            if (vw != null) {
                if (!vw.isAliveNow()) {
                    this.map.remove(key, vw);
                }
            }
        });
        return Future.succeededFuture();
    }

    @Override
    public Future<Map<K, V>> getSnapshotMap() {
        ConcurrentMap<K, V> snapshot = new ConcurrentHashMap<>();
        synchronized (this.map) {
            this.map.keySet().forEach(key -> {
                ValueWrapper<V> vw = this.map.get(key);
                if (vw != null) {
                    if (vw.isAliveNow()) {
                        snapshot.put(key, vw.getValue());
                    }
                }
            });
        }
        return Future.succeededFuture(Collections.unmodifiableMap(snapshot));
    }
}
