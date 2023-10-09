package io.github.sinri.keel.redis;

import io.github.sinri.keel.facade.Keel;
import io.github.sinri.keel.redis.mixin.*;
import io.vertx.core.Future;
import io.vertx.redis.client.Redis;
import io.vertx.redis.client.RedisAPI;
import io.vertx.redis.client.RedisConnection;
import io.vertx.redis.client.RedisOptions;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @since 3.0.5
 */
public class RedisKit implements RedisApiMixin, RedisScalarMixin, RedisListMixin, RedisBitMixin, RedisHashMixin, RedisSetMixin, RedisOrderedSetMixin {
    private final Redis client;
    private final AtomicReference<RedisConnection> redisConnectionRef = new AtomicReference<>();

    public RedisKit(String redisInstanceKey) {
        String url = Keel.getConfiguration().readString("redis", redisInstanceKey, "url");
        Objects.requireNonNull(url);
        this.client = Redis.createClient(Keel.getVertx(), new RedisOptions()
                .setConnectionString(url)
                .setMaxPoolSize(16)
                .setMaxWaitingHandlers(32)
                .setMaxPoolWaiting(24)
                .setPoolCleanerInterval(5000)
        );
    }

    public Redis getClient() {
        return client;
    }

    @Override
    public Future<RedisAPI> api() {
        // since 20230901, try to resolve pool max over issue
        if (redisConnectionRef.get() == null) {
            return getClient().connect()
                    .compose(redisConnection -> {
                        redisConnectionRef.set(redisConnection);
                        return Future.succeededFuture(RedisAPI.api(redisConnectionRef.get()));
                    });
        } else {
            return Future.succeededFuture(RedisAPI.api(redisConnectionRef.get()));
        }
    }
}
