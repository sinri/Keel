package io.github.sinri.keel.cache.impl;

import io.github.sinri.keel.cache.KeelAsyncCacheInterface;
import io.github.sinri.keel.cache.ValueWrapper;
import io.vertx.core.Future;

import javax.annotation.Nonnull;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;

public class KeelCacheBet<K, V> implements KeelAsyncCacheInterface<K, V> {
    private final ConcurrentMap<K, ValueWrapper<V>> map;

    public KeelCacheBet() {
        this.map = new ConcurrentHashMap<>();
    }

    @Override
    public Future<Void> save(@Nonnull K key, V value, long lifeInSeconds) {
        this.map.put(key, new ValueWrapper<>(value, lifeInSeconds));
        return Future.succeededFuture();
    }

    @Override
    public Future<V> read(@Nonnull K key) {
        ValueWrapper<V> vw = this.map.get(key);
        if (vw == null || !vw.isAliveNow()) {
            return Future.failedFuture(new NotCached(key.toString()));
        }
        return Future.succeededFuture(vw.getValue());
    }

    @Override
    public Future<V> read(@Nonnull K key, V fallbackValue) {
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
    public Future<V> read(@Nonnull K key, Function<K, Future<V>> generator, long lifeInSeconds) {
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
    public Future<Void> remove(@Nonnull K key) {
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
    public Future<ConcurrentMap<K, V>> getSnapshotMap() {
        ConcurrentMap<K, V> snapshot = new ConcurrentHashMap<>();
        this.map.keySet().forEach(key -> {
            ValueWrapper<V> vw = this.map.get(key);
            if (vw != null) {
                if (vw.isAliveNow()) {
                    snapshot.put(key, vw.getValue());
                }
            }
        });
        return Future.succeededFuture(snapshot);
    }
}
