package io.github.sinri.keel.facade.async;

import io.vertx.core.Future;
import io.vertx.core.Promise;
import org.jetbrains.annotations.Nullable;

import static io.github.sinri.keel.facade.KeelInstance.Keel;

/**
 * 将延时执行转换成Future供compose使用。
 * Promise 真是个好东西！
 *
 * @since 2.9
 */
public class FutureSleep {
    static Future<Void> call(long time) {
        return call(time, null);
    }

    static Future<Void> call(long time, @Nullable Promise<Void> interrupter) {
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
