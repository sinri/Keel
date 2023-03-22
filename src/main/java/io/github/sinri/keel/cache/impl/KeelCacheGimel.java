package io.github.sinri.keel.cache.impl;

import io.github.sinri.keel.cache.KeelAsyncEverlastingCacheInterface;
import io.vertx.core.Future;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.BooleanSupplier;

/**
 * 
 * 
 * @since 2.9
 */
public class KeelCacheGimel<K, V> implements KeelAsyncEverlastingCacheInterface<K, V> {
    private final Lock lock;
    private final Map<K, V> map;
    private long lockWaitMs = 100;

    public KeelCacheGimel() {
        lock = new ReentrantLock();
        map = new HashMap<>();
    }

    @Override public long getLockWaitMs() {
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
    public Future<V> read(K k, V v) {
        AtomicReference<V> vRef = new AtomicReference<>();
        return actionInLock(() -> {
            var x = map.get(k);
            if (x != null) {
                vRef.set(x);
            } else {
                vRef.set(v);
            }
            return true;
        })
                .compose(unlocked -> Future.succeededFuture(vRef.get()));
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

    /**
     * @param newEntries new map of entries
     * @since 2.9.4 no longer implemented by replace map
     */
    @Override
    public Future<Void> replaceAll(Map<K, V> newEntries) {
        return actionInLock(() -> {
            Set<K> ks = newEntries.keySet();
            map.putAll(newEntries);
            map.keySet().forEach(k -> {
                if (!ks.contains(k)) {
                    map.remove(k);
                }
            });
            return true;
        });
    }

    @Override
    public Map<K, V> getSnapshotMap() {
        return Collections.unmodifiableMap(map);
    }
}
