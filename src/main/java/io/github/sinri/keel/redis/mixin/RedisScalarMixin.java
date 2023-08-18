package io.github.sinri.keel.redis.mixin;

import io.vertx.core.Future;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @since 3.0.5
 * 在本类中，String属于Scalar，但Scalar可以被解释成数字甚至bitmap。
 */
public interface RedisScalarMixin extends RedisApiMixin {
    default Future<Void> setScalarToKeyForSeconds(String key, String value, Integer exInSecond) {
        return this.setScalarToKeyForSeconds(key, value, exInSecond, SetMode.None);
    }

    /**
     * SET key value [EX seconds|PX milliseconds|KEEPTTL] [NX|XX] [GET]
     */
    default Future<Void> setScalarToKeyForSeconds(String key, String value, Integer exInSecond, SetMode setMode) {
        return setScalarToKey(key, value, exInSecond, null, setMode);
    }

    default Future<Void> setScalarToKeyForMilliseconds(String key, String value, Long milliseconds) {
        return this.setScalarToKeyForMilliseconds(key, value, milliseconds, SetMode.None);
    }

    /**
     * SET key value [EX seconds|PX milliseconds|KEEPTTL] [NX|XX] [GET]
     */
    default Future<Void> setScalarToKeyForMilliseconds(String key, String value, Long milliseconds, SetMode setMode) {
        return setScalarToKey(key, value, null, milliseconds, setMode);
    }

    default Future<Void> setScalarToKeyForever(String key, String value) {
        return this.setScalarToKeyForever(key, value, SetMode.None);
    }


    default Future<Void> setScalarToKeyForever(String key, String value, SetMode setMode) {
        return setScalarToKey(key, value, null, null, setMode);
    }

    /**
     * SET key value [EX seconds|PX milliseconds|KEEPTTL] [NX|XX] [GET]
     */
    private Future<Void> setScalarToKey(
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
     * GET key
     * Redis Get 命令用于获取指定 key 的值。 返回与 key 相关联的字符串值。
     *
     * @return 如果键 key 不存在， 那么返回特殊值 nil 。
     * 如果键 key 的值不是字符串类型， 返回错误， 因为 GET 命令只能用于字符串值。
     */
    default Future<String> getString(String key) {
        return api().compose(api -> {
            return api.get(key).compose(response -> {
                if (response == null) {
                    return Future.succeededFuture();
                }
                return Future.succeededFuture(response.toString());
            });
        });
    }

    /**
     * GETRANGE key start end
     * GETRANGE 命令返回存储在 key 中的字符串的子串，由 start 和 end 偏移决定(都包括在内)。负数偏移提供相对字符串结尾的偏移。所以， -1 表示最后一个字符， -2 表示倒数第二个字符，以此类推。
     * GETRANGE 通过将结果范围限制为字符串的实际长度来处理超出范围的请求。
     * Warning: GETRANGE 是改名而来，在 Redis2.0 以前版本叫做 SUBSTR 。
     *
     * @return 截取得到的子字符串。
     */
    default Future<String> getSubstring(String key, int start, int end) {
        return api().compose(api -> {
            return api.getrange(key, String.valueOf(start), String.valueOf(end)).compose(response -> {
                return Future.succeededFuture(response.toString());
            });
        });
    }

    /**
     * GETSET key value
     * 将键 key 的值设为 value ， 并返回键 key 在被设置之前的旧值。
     * 返回给定键 key 的旧值。
     * 如果键 key 没有旧值，也即是说，键 key 在被设置之前并不存在， 那么命令返回 nil 。
     * 当键 key 存在但不是字符串类型时，命令返回一个错误。
     *
     * @return the old value stored at key, or nil when key did not exist.
     */
    default Future<String> replaceString(String key, String newValue) {
        return api().compose(api -> {
            return api.getset(key, newValue).compose(response -> {
                if (response == null) {
                    return Future.succeededFuture();
                }
                return Future.succeededFuture(response.toString());
            });
        });
    }

    /**
     * INCR key
     * Redis INCR 命令将 key 中储存的数字值增一。
     * 如果 key 不存在，那么 key 的值会先被初始化为 0 ，然后再执行 INCR 操作。
     * 如果值包含错误的类型，或字符串类型的值不能表示为数字，那么返回一个错误 ERR ERR hash value is not an integer。
     * 本操作的值限制在 64 位(bit)有符号数字表示之内。
     * Note: 本质上这是一个字符串操作，因为Redis没有专门的整数类型。存储在 key 中的字符串被转换为十进制有符号整数，在此基础上加1。
     *
     * @return 执行 INCR 命令之后 key 的值。
     */
    default Future<Long> increment(String key) {
        return api().compose(api -> {
            return api.incr(key).compose(response -> {
                return Future.succeededFuture(response.toLong());
            });
        });
    }

    /**
     * INCRBY key increment
     * Redis INCRBY 命令将 key 中储存的数字加上指定的增量值。
     * 如果 key 不存在，那么 key 的值会先被初始化为 0 ，然后再执行 INCRBY 命令。
     * 如果值包含错误的类型，或字符串类型的值不能表示为数字，那么返回一个错误 ERR ERR hash value is not an integer。
     * 本操作的值限制在 64 位(bit)有符号数字表示之内。
     * 关于递增(increment) / 递减(decrement)操作的更多信息， 请参见 INCR 命令的文档。
     * @return 命令执行之后 key 中 存储的值。
     */
    default Future<Long> increment(String key, long x) {
        return api().compose(api -> {
            return api.incrby(key, String.valueOf(x)).compose(response -> {
                return Future.succeededFuture(response.toLong());
            });
        });
    }

    /**
     * INCRBYFLOAT key increment
     * 为键 key 中储存的值加上浮点数增量 increment ，key 中的浮点数是以字符串形式存储的。
     * 如果键 key 不存在， 那么 INCRBYFLOAT 会先将键 key 的值设为 0 ， 然后再执行加法操作。
     * 如果命令执行成功， 那么键 key 的值会被更新为执行加法计算之后的新值， 并且新值会以字符串的形式返回给调用者。
     * 无论是键 key 的值还是增量 increment ， 都可以使用像 2.0e7 、 3e5 、 90e-2 那样的指数符号(exponential notation)来表示， 但是， 执行 INCRBYFLOAT 命令之后的值总是以同样的形式储存， 也即是， 它们总是由一个数字， 一个（可选的）小数点和一个任意长度的小数部分组成（比如 3.14 、 69.768 ，诸如此类)， 小数部分尾随的 0 会被移除， 如果可能的话， 命令还会将浮点数转换为整数（比如 3.0 会被保存成 3 ）。
     * 此外， 无论加法计算所得的浮点数的实际精度有多长， INCRBYFLOAT 命令的计算结果最多只保留小数点的后十七位。
     * 当以下任意一个条件发生时， 命令返回一个错误：
     * 键 key 的值不是字符串类型(因为 Redis 中的数字和浮点数都以字符串的形式保存，所以它们都属于字符串类型）；
     * 键 key 当前的值或者给定的增量 increment 不能被解释(parse)为双精度浮点数。
     * @return 在加上增量 increment 之后， 键 key 的值。
     */
    default Future<Double> increment(String key, double x) {
        return api().compose(api -> {
            return api.incrbyfloat(key, String.valueOf(x)).compose(response -> {
                return Future.succeededFuture(response.toDouble());
            });
        });
    }

    /**
     * DECR key
     * 为键 key 储存的数字值减去一。
     * 如果键 key 不存在， 那么键 key 的值会先被初始化为 0 ， 然后再执行 DECR 操作。
     * 如果键 key 储存的值不能被解释为数字， 那么 DECR 命令将返回一个错误。
     * 本操作的值限制在 64 位(bit)有符号数字表示之内。
     *
     * @return 执行操作之后key中的值
     */
    default Future<Long> decrement(String key) {
        return api().compose(api -> {
            return api.decr(key).compose(response -> {
                return Future.succeededFuture(response.toLong());
            });
        });
    }

    /**
     * DECRBY key decrement
     * 将键 key 储存的整数值减去减量 decrement 。
     * 如果键 key 不存在， 那么键 key 的值会先被初始化为 0 ， 然后再执行 DECRBY 命令。
     * 如果键 key 储存的值不能被解释为数字， 那么 DECRBY 命令将返回一个错误。
     * 本操作的值限制在 64 位(bit)有符号数字表示之内。
     *
     * @return 键在执行减法操作之后的值。
     */
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

    /**
     * SETNX key value
     * Redis Setnx（ SET if Not eXists ）命令在指定的 key 不存在时，为 key 设置指定的值，这种情况下等同 SET 命令。当 key存在时，什么也不做。
     *
     * @return 1 如果key被设置了 ; 0 如果key没有被设置.
     */
    default Future<Integer> setStringIfKeyNotExists(String key, String value) {
        return api().compose(api -> {
            return api.setnx(key, value).compose(response -> {
                return Future.succeededFuture(response.toInteger());
            });
        });
    }

    /**
     * SETRANGE key offset value
     * SETRANGE 命令从偏移量 offset 开始， 用 value 参数覆盖键 key 中储存的字符串值。如果键 key 不存在，当作空白字符串处理。
     * SETRANGE 命令可以保证key中的字符串足够长，以便将 value 覆盖到key中。
     * 如果键 key 中原来所储存的字符串长度比偏移量小(比如字符串只有 5 个字符长，但要设置的 offset 是 10 )， 那么原字符和偏移量之间的空白将用零字节 "\x00" 进行填充。
     * 因为 Redis 字符串的大小被限制在 512 兆以内， 所以用户能够使用的最大偏移量为 229 - 1(536870911) ， 如果要使用比这更大的空间， 可以用多个 key 。
     * 注意: 当生成一个很长的字符串时， 因 Redis 需要分配内存空间， 这种操作有时候可能会造成服务器阻塞。 在2010年生产的Macbook Pro上， 设置偏移量为 536870911(分配512MB 内存)将耗费约 300 毫秒， 设置偏移量为 134217728(分配128MB 内存)将耗费约 80 毫秒， 设置偏移量 33554432(分配32MB 内存)将耗费约 30 毫秒， 设置偏移量为 8388608(分配8MB 内存)将耗费约 8 毫秒。
     *
     * @param offset since 0
     * @return 被修改之后的字符串长度。
     */
    default Future<Integer> setSubstring(String key, int offset, String value) {
        return api().compose(api -> {
            return api.setrange(key, String.valueOf(offset), value).compose(response -> {
                return Future.succeededFuture(response.toInteger());
            });
        });
    }

    /**
     * STRLEN key
     * Redis Strlen 命令用于获取指定 key 所储存的字符串值的长度。当 key 储存的不是字符串类型时，返回错误。
     *
     * @return 字符串的长度，key 不存在时，返回 0.
     */
    default Future<Integer> getStringLength(String key) {
        return api().compose(api -> {
            return api.strlen(key).compose(response -> {
                return Future.succeededFuture(response.toInteger());
            });
        });
    }

    // TODO
    //  MGET key [key ...]
    //  MSET key value [key value ...]
    //  MSETNX key value [key value ...]
    //  SETEX key seconds value
    //  PSETEX key milliseconds value
    //  STRALGO LCS algo-specific-argument [algo-specific-argument ...]
}
