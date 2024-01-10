package io.github.sinri.keel.test.lab.redis;

import io.github.sinri.keel.facade.async.KeelAsyncKit;
import io.github.sinri.keel.redis.RedisKit;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.VertxOptions;

import java.util.Date;
import java.util.List;

import static io.github.sinri.keel.facade.KeelInstance.Keel;

public class RedisTest {
    public static void main(String[] args) {
        Keel.getConfiguration().loadPropertiesFile("config.properties");
        Keel.initializeVertxStandalone(new VertxOptions());

        RedisKit kit = new RedisKit("test");
        Future.succeededFuture()
                .compose(start_here -> {
                    return t2(kit);
                })
                .onFailure(throwable -> {
                    throwable.printStackTrace();
                })
                .eventually(() -> {
                    return Keel.gracefullyClose(Promise::complete);
                });
    }

    private static Future<Void> t2(RedisKit kit) {
        String key = "test2";
        return kit.pushToListTail(key, "A")
                .compose(x -> {
                    System.out.println("pushed A, now " + x);

                    return kit.pushToListTail(key, List.of("B", "C"));
                })
                .compose(x -> {
                    System.out.println("pushed B and C, now " + x);

                    return kit.fetchListWithRange(key, 1, 2);
                })
                .compose(list -> {
                    for (var x : list) {
                        System.out.println("item: " + x);
                    }
                    return kit.deleteKey(key);
                })
                .compose(deleted -> {
                    System.out.println("deleted: " + deleted);
                    return Future.succeededFuture();
                });
    }

    private static Future<Void> t1(RedisKit kit) {
        String key = "test1";

        return kit.setScalarToKeyForSeconds(key, new Date().toString(), 5)
                .compose(written -> {
                    System.out.println("written");
                    return KeelAsyncKit.sleep(2000L);
                })
                .compose(slept -> {
                    return kit.getString(key).compose(value -> {
                        System.out.println("after 2s, value: " + value);
                        return KeelAsyncKit.sleep(2000L);
                    });
                })
                .compose(slept -> {
                    return kit.getString(key).compose(value -> {
                        System.out.println("after 2s, value: " + value);
                        return kit.deleteKey(key);
                    });
                })
                .compose(deleted -> {
                    System.out.println("deletion: " + deleted);
                    return kit.getString(key).compose(value -> {
                        System.out.println("after deletion, value: " + value);
                        return KeelAsyncKit.sleep(2000L);
                    });
                });
    }
}
