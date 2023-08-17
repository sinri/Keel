package io.github.sinri.keel.redis.mixin;

import io.vertx.core.Future;
import io.vertx.redis.client.Redis;
import io.vertx.redis.client.RedisAPI;

import java.util.List;
import java.util.Objects;

/**
 * @since 3.0.5
 */
public interface RedisApiMixin {
    Redis getClient();

    default Future<RedisAPI> api() {
        return getClient().connect().compose(redisConnection -> {
            RedisAPI api = RedisAPI.api(redisConnection);
            return Future.succeededFuture(api);
        });
    }

    default Future<Boolean> doesKeyExist(String key) {
        return api().compose(api -> {
            return api.exists(List.of(key))
                    .compose(response -> {
                        Objects.requireNonNull(response);
                        return Future.succeededFuture(response.toInteger() == 1);
                    });
        });
    }

    default Future<Integer> deleteKey(String key) {
        return api().compose(api -> {
            return api.del(List.of(key))
                    .compose(response -> {
                        return Future.succeededFuture(response.toInteger());
                    });
        });
    }

    default Future<ValueType> getValueTypeOfKey(String key) {
        return api().compose(api -> {
            return api.type(key)
                    .compose(response -> {
                        Objects.requireNonNull(response);
                        String x = response.toString();
                        return Future.succeededFuture(ValueType.valueOf(x));
                    });
        });
    }

    default Future<String> randomKey() {
        return api().compose(api -> {
            return api.randomkey().compose(response -> {
                if (response == null) {
                    return Future.succeededFuture(null);
                } else {
                    return Future.succeededFuture(response.toString());
                }
            });
        });
    }

    enum ValueType {
        string, list, set, zset, hash, stream, none
    }
}
