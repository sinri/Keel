package io.github.sinri.keel.facade.async;

import io.vertx.core.Future;
import io.vertx.core.Promise;

/**
 * 将延时执行转换成Future供compose使用。
 * Promise 真是个好东西！
 *
 * @since 2.9
 */
public class FutureSleep {
    static Future<Void> call(KeelTraitForVertxAsync keel, long time) {
        Promise<Void> promise = Promise.promise();
        if (time < 1) time = 1;
        keel.getVertx().setTimer(time, x -> promise.complete());
        return promise.future();
    }
}
