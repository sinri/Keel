package io.github.sinri.keel.cache;

import io.github.sinri.keel.cache.impl.KeelCacheAlef;
import io.github.sinri.keel.cache.impl.KeelCacheDummy;
import io.github.sinri.keel.facade.async.KeelAsyncKit;
import io.vertx.core.Future;

import javax.annotation.Nonnull;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;

/**
 * @param <K> class for key
 * @param <V> class for key
 * @since 1.9
 */
public interface KeelCacheInterface<K, V> {
    /**
     * @param <K> class for key
     * @param <V> class for value
     * @return A new instance of KeelCacheInterface created.
     * @since 1.9 Use CaffeineCacheKit as implementation by default.
     * @since 2.5 changed to use KeelCacheAlef
     */
    static <K, V> KeelCacheInterface<K, V> createDefaultInstance() {
        return new KeelCacheAlef<>();
    }

    /**
     * @since 2.6
     */
    static <K, V> KeelCacheInterface<K, V> getDummyInstance() {
        return new KeelCacheDummy<>();
    }

    /**
     * @since 2.8
     */
    long getDefaultLifeInSeconds();

    /**
     * @since 2.8
     */
    KeelCacheInterface<K, V> setDefaultLifeInSeconds(long lifeInSeconds);

    /**
     * Save an item (as key and value pair) into cache, keep it available for a certain time.
     *
     * @param key           key
     * @param value         value
     * @param lifeInSeconds The lifetime of the cache item, in seconds.
     */
    void save(@Nonnull K key, V value, long lifeInSeconds);

    /**
     * @since 2.8
     */
    default void save(@Nonnull K key, V value) {
        save(key, value, getDefaultLifeInSeconds());
    }

    /**
     * Read an available cached item with key, or return `fallbackValue` when not found.
     *
     * @param key           key
     * @param fallbackValue the certain value returned when not found
     * @return value of found available cached item, or `fallbackValue`
     */
    V read(@Nonnull K key, V fallbackValue);

    /**
     * Read an available cached item with key, or return `null` when not found.
     *
     * @param key key
     * @return value of found available cached item, or `null`
     */
    default V read(@Nonnull K key) {
        return this.read(key, null);
    }

    /**
     * Remove the cached item with key.
     *
     * @param key key
     */
    void remove(@Nonnull K key);

    /**
     * Remove all the cached items.
     */
    void removeAll();

    /**
     * clean up the entries that is not alive (expired, etc.)
     */
    void cleanUp();

    /**
     * @return ConcurrentMap K → V alive value only
     * @since 1.14
     */
    @Nonnull
    ConcurrentMap<K, V> getSnapshotMap();

    /**
     * @since 2.8
     */
    default Future<V> read(@Nonnull K key, Function<K, Future<V>> generator, long lifeInSeconds) {
        V existed = this.read(key);
        if (existed != null) {
            return Future.succeededFuture(existed);
        }
        return generator.apply(key)
                .compose(v -> {
                    this.save(key, v, lifeInSeconds);
                    return Future.succeededFuture(v);
                });
    }

    /**
     * Start an endless for cleaning up.
     * Use it manually if needed.
     *
     * @since 3.0.4
     */
    default void startEndlessCleanUp(long sleepTime) {
        KeelAsyncKit.endless(() -> {
            cleanUp();
            return KeelAsyncKit.sleep(sleepTime);
        });
    }
}
