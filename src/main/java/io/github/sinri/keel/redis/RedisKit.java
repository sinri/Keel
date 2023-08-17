package io.github.sinri.keel.redis;

import io.github.sinri.keel.facade.Keel;
import io.github.sinri.keel.redis.mixin.RedisApiMixin;
import io.github.sinri.keel.redis.mixin.RedisListMixin;
import io.github.sinri.keel.redis.mixin.RedisScalarMixin;
import io.vertx.redis.client.Redis;
import io.vertx.redis.client.RedisOptions;

import java.util.Objects;

/**
 * @since 3.0.5
 */
public class RedisKit implements RedisApiMixin, RedisScalarMixin, RedisListMixin {
    private final Redis client;

    public RedisKit(String redisInstanceKey) {
        String url = Keel.getConfiguration().readString("redis", redisInstanceKey, "url");
        Objects.requireNonNull(url);
        this.client = Redis.createClient(Keel.getVertx(), new RedisOptions()
                .setConnectionString(url)
                .setMaxPoolSize(16)
                .setMaxWaitingHandlers(32)
        );
    }

    public Redis getClient() {
        return client;
    }


}
