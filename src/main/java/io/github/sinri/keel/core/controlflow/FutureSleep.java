package io.github.sinri.keel.core.controlflow;

import io.github.sinri.keel.Keel;
import io.vertx.core.Future;
import io.vertx.core.Promise;

/**
 * @since 2.8.1
 */
public class FutureSleep {
    public static Future<Void> call(long time) {
        Promise<Void> promise = Promise.promise();
        Keel.getVertx().setTimer(time, x -> promise.complete());
        return promise.future();
    }
}
