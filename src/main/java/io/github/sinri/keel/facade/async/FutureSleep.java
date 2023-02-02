package io.github.sinri.keel.facade.async;

import io.github.sinri.keel.facade.Keel;
import io.vertx.core.Future;
import io.vertx.core.Promise;

/**
 * 将延时执行转换成Future供compose使用。
 * Promise 真是个好东西！
 *
 * @since 2.9
 */
public class FutureSleep {
    static Future<Void> call(long time) {
//        Promise<Void> promise = Promise.promise();
//        if (time < 1) time = 1;
//        Keel.getVertx().setTimer(time, x -> promise.complete());
//        return promise.future();
        return call(time, null);
    }

    static Future<Void> call(long time, Promise<Void> interrupter) {
        Promise<Void> promise = Promise.promise();
        if (time < 1) time = 1;
        long timer_id = Keel.getVertx().setTimer(time, timerID -> {
            promise.complete();
        });
        if (interrupter != null) {
            interrupter.future().onSuccess(interrupted -> {
                Keel.getVertx().cancelTimer(timer_id);
                promise.tryComplete();
            });
        }
        return promise.future();
    }
}
