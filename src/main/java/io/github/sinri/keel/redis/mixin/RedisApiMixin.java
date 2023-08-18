package io.github.sinri.keel.redis.mixin;

import io.vertx.core.Future;
import io.vertx.redis.client.Redis;
import io.vertx.redis.client.RedisAPI;

import java.util.ArrayList;
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

    /**
     * EXISTS key [key ...]
     * 从 Redis 3.0.3 起可以一次检查多个 key 是否存在。这种情况下，返回待检查 key 中存在的 key 的个数。检查单个 key 返回 1 或 0 。
     * 注意：如果相同的 key 在参数列表中出现了多次，它会被计算多次。所以，如果somekey存在, EXISTS somekey somekey 命令返回 2。
     */
    default Future<Boolean> doesKeyExist(String key) {
        return api().compose(api -> {
            return api.exists(List.of(key))
                    .compose(response -> {
                        Objects.requireNonNull(response);
                        return Future.succeededFuture(response.toInteger() == 1);
                    });
        });
    }

    /**
     * EXISTS key [key ...]
     * 从 Redis 3.0.3 起可以一次检查多个 key 是否存在。这种情况下，返回待检查 key 中存在的 key 的个数。检查单个 key 返回 1 或 0 。
     * 注意：如果相同的 key 在参数列表中出现了多次，它会被计算多次。所以，如果somekey存在, EXISTS somekey somekey 命令返回 2。
     */
    default Future<Integer> countExistedKeys(List<String> keys) {
        return api().compose(api -> {
            return api.exists(keys)
                    .compose(response -> {
                        Objects.requireNonNull(response);
                        return Future.succeededFuture(response.toInteger());
                    });
        });
    }

    /**
     * DEL key [key ...]
     * Redis DEL 命令用于删除给定的一个或多个 key 。
     * 不存在的 key 会被忽略。
     *
     * @return 被删除 key 的数量。
     */
    default Future<Integer> deleteKeys(List<String> keys) {
        return api().compose(api -> {
            return api.del(keys)
                    .compose(response -> {
                        return Future.succeededFuture(response.toInteger());
                    });
        });
    }

    default Future<Integer> deleteKey(String key) {
        return deleteKeys(List.of(key));
    }

    /**
     * UNLINK key [key ...]
     * Redis UNLINK 命令跟 DEL 命令十分相似：用于删除指定的 key 。就像 DEL 一样，如果 key 不存在，则将其忽略。但是，该命令会执行命令之外的线程中执行实际的内存回收，因此它不是阻塞，而 DEL 是阻塞的。这就是命令名称的来源：UNLINK 命令只是将键与键空间断开连接。实际的删除将稍后异步进行。
     *
     * @return 被删除 key 的数量。
     */
    default Future<Integer> unlinkKeys(List<String> keys) {
        return api().compose(api -> {
            return api.unlink(keys)
                    .compose(response -> {
                        return Future.succeededFuture(response.toInteger());
                    });
        });
    }

    default Future<Integer> unlinkKey(String key) {
        return unlinkKeys(List.of(key));
    }

    /**
     * TYPE key
     * 以字符串的形式返回存储在 key 中的值的类型。
     * 可返回的类型是: string, list, set, zset,hash 和 stream。
     */
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

    /**
     * RANDOMKEY
     * Redis RANDOMKEY 命令从当前数据库中随机返回一个 key 。
     */
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

    /**
     * EXPIRE key seconds
     * 设置 key 的过期时间（seconds）。 设置的时间过期后，key 会被自动删除。带有超时时间的 key 通常被称为易失的(volatile)。
     * 超时时间只能使用删除 key 或者覆盖 key 的命令清除，包括 DEL, SET, GETSET 和所有的 *STORE 命令。 对于修改 key 中存储的值，而不是用新值替换旧值的命令，不会修改超时时间。例如，自增 key 中存储的值的 INCR , 向list中新增一个值 LPUSH, 或者修改 hash 域的值 HSET ，这些都不会修改 key 的过期时间。
     * 通过使用 PERSIST 命令把 key 改回持久的(persistent) key，这样 key 的过期时间也可以被清除。
     * key使用 RENAME 改名后，过期时间被转移到新 key 上。
     * 已存在的旧 key 使用 RENAME 改名，那么新 key 会继承所有旧 key 的属性。例如，一个名为 KeyA 的 key 使用命令 RENAME Key_B Key_A 改名，新的 KeyA 会继承包括超时时间在内的所有 Key_B 的属性。
     * 特别注意，使用负值调用 EXPIRE/PEXPIRE 或使用过去的时间调用 EXPIREAT/PEXPIREAT ，那么 key 会被删除 deleted 而不是过期。 (因为, 触发的key event 将是 del, 而不是 expired).
     */
    default Future<Void> expire(String key, int seconds) {
        return api().compose(api -> {
            return api.expire(List.of(key, String.valueOf(seconds)))
                    .compose(response -> {
                        return Future.succeededFuture();
                    });
        });
    }

    /**
     * EXPIREAT key timestamp
     * 详细语义功能说明可以参考 EXPIRE。
     * 使用过去的时间戳将会立即删除该 key。
     *
     * @param unixTimestampInSecond 绝对 Unix 时间戳 (自1970年1月1日以来的秒数)
     */
    default Future<Void> expireAt(String key, int unixTimestampInSecond) {
        return api().compose(api -> {
            return api.expireat(List.of(key, String.valueOf(unixTimestampInSecond)))
                    .compose(response -> {
                        return Future.succeededFuture();
                    });
        });
    }

    /**
     * PEXPIRE key milliseconds
     * PEXPIRE 跟 EXPIRE 基本一样，只是过期时间单位是毫秒。
     */
    default Future<Void> expireInMillisecond(String key, long milliseconds) {
        return api().compose(api -> {
            return api.pexpire(List.of(key, String.valueOf(milliseconds)))
                    .compose(response -> {
                        return Future.succeededFuture();
                    });
        });
    }

    /**
     * PEXPIREAT key milliseconds-timestamp
     * Redis PEXPIREAT 命令用于设置 key 的过期时间，时间的格式是uinx时间戳并精确到毫秒。
     */
    default Future<Void> expireAtInMillisecond(String key, long unixTimestampInMilliseconds) {
        return api().compose(api -> {
            return api.pexpireat(List.of(key, String.valueOf(unixTimestampInMilliseconds)))
                    .compose(response -> {
                        return Future.succeededFuture();
                    });
        });
    }

    /**
     * PTTL key
     * Redis PTTL 命令以毫秒为单位返回 key 的剩余过期时间。
     */
    default Future<Long> getTTLInMillisecond(String key) {
        return api().compose(api -> {
            return api.pttl(key)
                    .compose(response -> {
                        var ttl = response.toLong();
                        if (ttl < 0) {
                            // Redis 2.6 之前的版本如果 key 不存在或者 key 没有关联超时时间则返回 -1 。
                            //Redis 2.8 起：//key 不存在返回 -2 //key 存在但是没有关联超时时间返回 -1
                            return Future.failedFuture(new RuntimeException("key 不存在或者 key 没有关联超时时间"));
                        }
                        return Future.succeededFuture();
                    });
        });
    }

    /**
     * TTL key
     * Redis TTL 命令以秒为单位返回 key 的剩余过期时间。用户客户端检查 key 还可以存在多久。
     */
    default Future<Long> getTTLInSecond(String key) {
        return api().compose(api -> {
            return api.ttl(key)
                    .compose(response -> {
                        var ttl = response.toLong();
                        if (ttl < 0) {
                            // Redis 2.6 之前的版本如果 key 不存在或者 key 没有关联超时时间则返回 -1 。
                            //Redis 2.8 起：//key 不存在返回 -2 //key 存在但是没有关联超时时间返回 -1
                            return Future.failedFuture(new RuntimeException("key 不存在或者 key 没有关联超时时间"));
                        }
                        return Future.succeededFuture();
                    });
        });
    }

    /**
     * PERSIST key
     * Redis PERSIST 命令用于删除给定 key 的过期时间，使得 key 永不过期。
     */
    default Future<Void> persist(String key) {
        return api().compose(api -> {
            return api.persist(key).compose(response -> {
                return Future.succeededFuture();
            });
        });
    }

    /**
     * KEYS pattern
     * Redis KEYS 命令用于查找所有匹配给定模式 pattern 的 key 。
     * 尽管这个操作的时间复杂度是 O(N)，但是常量时间相当小。
     * 例如，在一个普通笔记本上跑 Redis，扫描 100 万个 key 只要40毫秒。
     * Warning: 生产环境使用 KEYS 命令需要非常小心。在大的数据库上执行命令会影响性能。
     * 这个命令适合用来调试和特殊操作，像改变键空间布局。
     * 不要在你的代码中使用 KEYS 。如果你需要一个寻找键空间中的key子集，考虑使用 SCAN 或 sets。
     * 匹配模式:
     * h?llo 匹配 hello, hallo 和 hxllo
     * h*llo 匹配 hllo 和 heeeello
     * h[ae]llo 匹配 hello and hallo, 不匹配 hillo
     * h[^e]llo 匹配 hallo, hbllo, ... 不匹配 hello
     * h[a-b]llo 匹配 hallo 和 hbllo
     * 使用 \ 转义你想匹配的特殊字符。
     */
    default Future<List<String>> keys(String pattern) {
        return api().compose(api -> {
            return api.keys(pattern).compose(response -> {
                List<String> list = new ArrayList<>();
                response.forEach(x -> {
                    list.add(x.toString());
                });
                return Future.succeededFuture(list);
            });
        });
    }

    /**
     * RENAME key newkey
     * 修改 key 的名字为 newkey 。若key 不存在返回错误。
     * 在集群模式下，key 和newkey 需要在同一个 hash slot。key 和newkey有相同的 hash tag 才能重命名。
     * 如果 newkey 存在则会被覆盖，此种情况隐式执行了 DEL 操作，所以如果要删除的key的值很大会有一定的延时，即使RENAME 本身是常量时间复杂度的操作。
     * 在集群模式下，key 和newkey 需要在同一个 hash slot。key 和newkey有相同的 hash tag 才能重命名。
     */
    default Future<Void> renameKey(String oldKey, String newKey) {
        return api().compose(api -> {
            return api.rename(oldKey, newKey).compose(response -> {
                if ("OK".equals(response.toString())) {
                    return Future.succeededFuture();
                } else {
                    throw new RuntimeException(response.toString());
                }
            });
        });
    }

    /**
     * RENAMENX key newkey
     * Redis Renamenx 命令用于在新的 key 不存在时修改 key 的名称 。若 key 不存在返回错误。
     * 在集群模式下，key 和newkey 需要在同一个 hash slot。key 和newkey有相同的 hash tag 才能重命名。
     */
    default Future<Void> renameKeyIfNewKeyNotExists(String oldKey, String newKey) {
        return api().compose(api -> {
            return api.renamenx(oldKey, newKey).compose(response -> {
                if ("OK".equals(response.toString())) {
                    return Future.succeededFuture();
                } else {
                    throw new RuntimeException(response.toString());
                }
            });
        });
    }

    /**
     * TOUCH key [key ...]
     * 修改指定 key 的 最后访问时间。忽略不存在的 key。
     *
     * @return 被更新的 key 个数
     */
    default Future<Integer> touch(List<String> keys) {
        return api().compose(api -> {
            return api.touch(keys).compose(response -> {
                return Future.succeededFuture(response.toInteger());
            });
        });
    }

    // todo
    //  DUMP key
    //  RESTORE key ttl serialized-value [REPLACE] [ABSTTL] [IDLETIME seconds] [FREQ frequency]
    //  MIGRATE host port key|"" destination-db timeout [COPY] [REPLACE] [AUTH password] [AUTH2 username password] [KEYS key [key ...]]
    //  MOVE key db
    //  OBJECT subcommand [arguments [arguments ...]]
    //  SCAN cursor [MATCH pattern] [COUNT count] [TYPE type]
    //  SORT key [BY pattern] [LIMIT offset count] [GET pattern [GET pattern ...]] [ASC|DESC] [ALPHA] [STORE destination]
    //  WAIT numreplicas timeout
}
