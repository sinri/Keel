package io.github.sinri.keel.core.controlflow;

import io.github.sinri.keel.Keel;
import io.vertx.core.Future;
import io.vertx.core.Promise;

import java.util.function.Supplier;

/**
 * Repeat execute a functional block and get a future for boolean to decide whether stop or not.
 *
 * @see FutureRepeat
 * @since 2.8 Future Recursion Implementation
 * @since 2.9 changed to use Promise to avoid Thread Blocking Issue
 * @since 2.9.3 因为循环体里的未来返回值语义并不明确……所以……换用 io.github.sinri.keel.core.controlflow.FutureRepeat 吧。
 */
@Deprecated(since = "2.9.3")
public class FutureUntil {
    private final Supplier<Future<Boolean>> singleRecursionForShouldStopSupplier;

    private FutureUntil(Supplier<Future<Boolean>> singleRecursionForShouldStopSupplier) {
        this.singleRecursionForShouldStopSupplier = singleRecursionForShouldStopSupplier;
    }

    public static Future<Void> call(Supplier<Future<Boolean>> singleRecursionForShouldStopSupplier) {
        Promise<Void> promise = Promise.promise();
        new FutureUntil(singleRecursionForShouldStopSupplier).routine(promise);
        return promise.future();
    }

    private void routine(Promise<Void> finalPromise) {
        singleRecursionForShouldStopSupplier.get()
                .andThen(shouldStopAR -> {
                    if (shouldStopAR.succeeded()) {
                        var shouldStop = shouldStopAR.result();
                        if (shouldStop) {
                            finalPromise.complete();
                        } else {
                            Keel.getVertx().setTimer(1L, x -> routine(finalPromise));
                        }
                    } else {
                        finalPromise.fail(shouldStopAR.cause());
                    }
                });
    }

}
