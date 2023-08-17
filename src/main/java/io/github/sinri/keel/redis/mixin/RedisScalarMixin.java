package io.github.sinri.keel.redis.mixin;

import io.vertx.core.Future;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @since 3.0.5
 */
public interface RedisScalarMixin extends RedisApiMixin {
    default Future<Void> setScalarToKeyForSeconds(String key, String value, Integer exInSecond) {
        return this.setScalarToKeyForSeconds(key, value, exInSecond, SetMode.None);
    }

    /**
     * SET key value [EX seconds|PX milliseconds|KEEPTTL] [NX|XX] [GET]
     */
    default Future<Void> setScalarToKeyForSeconds(String key, String value, Integer exInSecond, SetMode setMode) {
        return setToKey(key, value, exInSecond, null, setMode);
    }

    default Future<Void> setScalarToKeyForMilliseconds(String key, String value, Long milliseconds) {
        return this.setScalarToKeyForMilliseconds(key, value, milliseconds, SetMode.None);
    }

    /**
     * SET key value [EX seconds|PX milliseconds|KEEPTTL] [NX|XX] [GET]
     */
    default Future<Void> setScalarToKeyForMilliseconds(String key, String value, Long milliseconds, SetMode setMode) {
        return setToKey(key, value, null, milliseconds, setMode);
    }

    default Future<Void> setScalarToKeyForever(String key, String value) {
        return this.setScalarToKeyForever(key, value, SetMode.None);
    }


    default Future<Void> setScalarToKeyForever(String key, String value, SetMode setMode) {
        return setToKey(key, value, null, null, setMode);
    }

    /**
     * SET key value [EX seconds|PX milliseconds|KEEPTTL] [NX|XX] [GET]
     */
    private Future<Void> setToKey(
            String key,
            String value,
            Integer EX,
            Long PX,
            SetMode setMode
    ) {
        List<String> args = new ArrayList<>();
        args.add(key);
        args.add(value);
        if (EX != null) {
            args.add("EX");
            args.add(String.valueOf(EX));
        } else if (PX != null) {
            args.add("PX");
            args.add(String.valueOf(PX));
        }
        if (setMode != SetMode.None) {
            args.add(setMode.name());
        }
        return api().compose(api -> {
            return api.set(args).compose(response -> {
                if (Objects.equals(response.toString(), "OK")) {
                    return Future.succeededFuture();
                } else {
                    return Future.failedFuture(new RuntimeException("SET Response is not OK but " + response));
                }
            });
        });
    }

    /**
     * Redis Get 命令用于获取指定 key 的值。 返回与 key 相关联的字符串值。
     *
     * @return 如果键 key 不存在， 那么返回特殊值 nil 。
     * 如果键 key 的值不是字符串类型， 返回错误， 因为 GET 命令只能用于字符串值。
     */
    default Future<String> getScalarWithKey(String key) {
        return api().compose(api -> {
            return api.get(key).compose(response -> {
                if (response == null) {
                    return Future.succeededFuture();
                }
                return Future.succeededFuture(response.toString());
            });
        });
    }

    default Future<Long> increment(String key) {
        return api().compose(api -> {
            return api.incr(key).compose(response -> {
                return Future.succeededFuture(response.toLong());
            });
        });
    }

    default Future<Long> increment(String key, long x) {
        return api().compose(api -> {
            return api.incrby(key, String.valueOf(x)).compose(response -> {
                return Future.succeededFuture(response.toLong());
            });
        });
    }

    default Future<Double> increment(String key, double x) {
        return api().compose(api -> {
            return api.incrbyfloat(key, String.valueOf(x)).compose(response -> {
                return Future.succeededFuture(response.toDouble());
            });
        });
    }

    default Future<Long> decrement(String key) {
        return api().compose(api -> {
            return api.decr(key).compose(response -> {
                return Future.succeededFuture(response.toLong());
            });
        });
    }

    default Future<Long> decrement(String key, long x) {
        return api().compose(api -> {
            return api.decrby(key, String.valueOf(x)).compose(response -> {
                return Future.succeededFuture(response.toLong());
            });
        });
    }

    /**
     * 为指定的 key 追加值。
     * 如果 key 已经存在并且是一个字符串， APPEND 命令将 value 追加到 key 原来的值的末尾。
     * 如果 key 不存在， APPEND 就简单地将给定 key 设为 value ，就像执行 SET key value 一样。
     *
     * @return 追加指定值之后， key 中字符串的长度。
     */
    default Future<Integer> appendForKey(String key, String tail) {
        return api().compose(api -> {
            return api.append(key, tail).compose(response -> {
                return Future.succeededFuture(response.toInteger());
            });
        });
    }

    enum SetMode {
        None,
        /**
         * NX: 只有键key不存在的时候才会设置key的值
         */
        NX,
        /**
         * XX: 只有键key存在的时候才会设置key的值
         */
        EX
    }
}
