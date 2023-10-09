package io.github.sinri.keel.redis;

import io.github.sinri.keel.cache.KeelAsyncCacheInterface;
import io.vertx.core.Future;

import java.util.Objects;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;

/**
 * @since 3.0.5
 */
public class KeelAsyncCacheWithRedis implements KeelAsyncCacheInterface<String, String> {
    private final RedisKit redisKit;

    public KeelAsyncCacheWithRedis(String redisInstanceKey) {
        this.redisKit = new RedisKit(redisInstanceKey);
    }

    @Override
    public Future<Void> save(String key, String value, long lifeInSeconds) {
        return this.redisKit.setScalarToKeyForSeconds(key, value, Math.toIntExact(lifeInSeconds));
    }

    @Override
    public Future<String> read(String key) {
        return this.redisKit.getString(key);
    }

    @Override
    public Future<String> read(String key, String fallbackValue) {
        return this.read(key).compose(s -> {
            return Future.succeededFuture(Objects.requireNonNullElse(s, fallbackValue));
        }, throwable -> {
            return Future.succeededFuture(fallbackValue);
        });
    }

    @Override
    public Future<String> read(String key, Function<String, Future<String>> generator, long lifeInSeconds) {
        return this.read(key).compose(s -> {
                    Objects.requireNonNull(s);
                    return Future.succeededFuture(s);
                })
                .recover(throwable -> {
                    return generator.apply(key)
                            .compose(v -> {
                                return save(key, v, lifeInSeconds)
                                        .recover(saveFailed -> {
                                            return Future.succeededFuture();
                                        })
                                        .compose(anyway -> {
                                            return Future.succeededFuture(v);
                                        });
                            });
                });
    }

    @Override
    public Future<Void> remove(String key) {
        return redisKit.deleteKey(key).compose(x -> {
            return Future.succeededFuture();
        });
    }

    @Override
    public Future<Void> removeAll() {
        // 似乎可以使用
        // FLUSHDB [ASYNC]
        // 清空当前 select 数据库中的所有 key。
        // 但看起来比较危险
        throw new UnsupportedOperationException();
    }

    @Override
    public Future<Void> cleanUp() {
        // redis 自带这个机制
        return Future.succeededFuture();
    }

    @Override
    public Future<ConcurrentMap<String, String>> getSnapshotMap() {
        // KEYS pattern
        // Redis KEYS 命令用于查找所有匹配给定模式 pattern 的 key 。
        // 尽管这个操作的时间复杂度是 O(N)，但是常量时间相当小。
        // 但看起来比较危险
        throw new UnsupportedOperationException();
    }
}
