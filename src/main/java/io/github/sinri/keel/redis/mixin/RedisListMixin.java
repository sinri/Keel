package io.github.sinri.keel.redis.mixin;

import io.vertx.core.Future;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @since 3.0.5
 */
public interface RedisListMixin extends RedisApiMixin {
    /**
     * 向存存储在 key 中的列表的尾部插入所有指定的值。
     * 如果 key 不存在，那么会创建一个空的列表然后再进行 push 操作。
     * 当 key 保存的不是列表，那么会返回一个错误。
     *
     * @return 执行 push 操作后的列表长度。
     */
    default Future<Integer> pushToListTail(String key, String element) {
        return api().compose(api -> {
            return api.rpush(List.of(key, element)).compose(response -> {
                return Future.succeededFuture(response.toInteger());
            });
        });
    }

    default Future<Integer> pushToListTail(String key, List<String> elements) {
        return api().compose(api -> {
            List<String> list = new ArrayList<>();
            list.add(key);
            list.addAll(elements);
            return api.rpush(list).compose(response -> {
                return Future.succeededFuture(response.toInteger());
            });
        });
    }

    /**
     * 将一个或多个值插入到列表key 的头部。
     * 如果 key 不存在，那么在进行 push 操作前会创建一个空列表。
     * 如果 key 对应的值不是 list 类型，那么会返回一个错误。
     */
    default Future<Integer> pushToListHead(String key, String element) {
        return api().compose(api -> {
            return api.lpush(List.of(key, element)).compose(response -> {
                return Future.succeededFuture(response.toInteger());
            });
        });
    }

    default Future<Integer> pushToListHead(String key, List<String> elements) {
        return api().compose(api -> {
            List<String> list = new ArrayList<>();
            list.add(key);
            list.addAll(elements);
            return api.lpush(list).compose(response -> {
                return Future.succeededFuture(response.toInteger());
            });
        });
    }

    /**
     * @return 用于返回存储在 key 中的列表长度。
     * 如果 key 不存在，则 key 被解释为一个空列表，返回 0 。
     * 如果 key 不是列表类型，返回一个错误。
     */
    default Future<Integer> getListLength(String key) {
        return api().compose(api -> {
            return api.llen(key).compose(response -> {
                return Future.succeededFuture(response.toInteger());
            });
        });
    }

    /**
     * @return 于删除并返回存储在 key 中的列表的第一个元素。
     */
    default Future<String> popFromListHead(String key) {
        return api().compose(api -> {
            return api.lpop(List.of(key)).compose(response -> {
                if (response == null) {
                    return Future.succeededFuture(null);
                }
                return Future.succeededFuture(response.toString());
            });
        });
    }

    default Future<String> popFromListTail(String key) {
        return api().compose(api -> {
            return api.rpop(List.of(key)).compose(response -> {
                if (response == null) {
                    return Future.succeededFuture(null);
                }
                return Future.succeededFuture(response.toString());
            });
        });
    }

    default Future<Void> trimList(String key, int start, int stop) {
        return api().compose(api -> {
            return api.ltrim(key, String.valueOf(start), String.valueOf(stop))
                    .compose(response -> {
                        Objects.requireNonNull(response);
                        if (Objects.equals("OK", response.toString())) {
                            return Future.succeededFuture();
                        } else {
                            return Future.failedFuture(new RuntimeException("NOT OK but " + response));
                        }
                    });
        });
    }

    default Future<List<String>> fetchListWithRange(String key, int start, int stop) {
        return api().compose(api -> {
            return api.lrange(key, String.valueOf(start), String.valueOf(stop))
                    .compose(response -> {
                        List<String> list = new ArrayList<>();
                        response.forEach(response1 -> {
                            list.add(response1.toString());
                        });

                        return Future.succeededFuture(list);
                    });
        });
    }
}
