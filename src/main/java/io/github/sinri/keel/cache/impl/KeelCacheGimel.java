package io.github.sinri.keel.cache.impl;

import io.github.sinri.keel.cache.KeelAsyncEverlastingCacheInterface;
import io.vertx.core.Future;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.BooleanSupplier;

/**
 * @param <K>
 * @param <V>
 * @since 2.8.1
 */
public class KeelCacheGimel<K, V> implements KeelAsyncEverlastingCacheInterface<K, V> {
    private final Lock lock;
    private Map<K, V> map;
    private long lockWaitMs = 100;

    public KeelCacheGimel() {
        lock = new ReentrantLock();
        map = new HashMap<>();
    }

    public long getLockWaitMs() {
        return lockWaitMs;
    }

    public KeelCacheGimel<K, V> setLockWaitMs(long lockWaitMs) {
        this.lockWaitMs = lockWaitMs;
        return this;
    }

    private Future<Void> actionInLock(BooleanSupplier resultSupplier) {
        return Future.succeededFuture()
                .compose(ready -> {
                    try {
                        var locked = lock.tryLock(getLockWaitMs(), TimeUnit.MILLISECONDS);
                        if (locked) {
                            return Future.failedFuture("locked");
                        }
                    } catch (InterruptedException e) {
                        return Future.failedFuture(e);
                    }
                    boolean result;
                    try {
                        result = resultSupplier.getAsBoolean();
                    } catch (Throwable throwable) {
                        result = false;
                    } finally {
                        lock.unlock();
                    }
                    if (result) {
                        return Future.succeededFuture();
                    } else {
                        return Future.failedFuture("action failed");
                    }
                });
    }

    @Override
    public Future<Void> save(K k, V v) {
        return actionInLock(() -> {
            map.put(k, v);
            return true;
        });
    }

    @Override
    public Future<Void> save(Map<K, V> appendEntries) {
        return actionInLock(() -> {
            map.putAll(appendEntries);
            return true;
        });
    }

    @Override
    public V read(K k, V v) {
        var x = map.get(k);
        if (x != null) {
            return x;
        } else {
            return v;
        }
    }

    @Override
    public Future<Void> remove(K key) {
        return actionInLock(() -> {
            map.remove(key);
            return true;
        });
    }

    @Override
    public Future<Void> remove(Collection<K> keys) {
        return actionInLock(() -> {
            keys.forEach(key -> map.remove(key));
            return true;
        });
    }

    @Override
    public Future<Void> removeAll() {
        return actionInLock(() -> {
            map.clear();
            return true;
        });
    }

    @Override
    public Future<Void> replaceAll(Map<K, V> newEntries) {
        return actionInLock(() -> {
            map = new HashMap<>(newEntries);
            return true;
        });
    }

    @Override
    public Map<K, V> getSnapshotMap() {
        return Collections.unmodifiableMap(map);
    }
}
