package io.github.sinri.keel.core.controlflow;

import io.github.sinri.keel.Keel;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.VertxOptions;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

/**
 * Repeat execute a functional block and get a future for boolean to decide whether stop or not.
 *
 * @since 2.8 Future Recursion Implementation
 * @since 2.9 changed to use Promise to avoid Thread Blocking Issue
 */
public class FutureUntil {
    private final Supplier<Future<Boolean>> singleRecursionForShouldStopSupplier;

    private FutureUntil(Supplier<Future<Boolean>> singleRecursionForShouldStopSupplier) {
        this.singleRecursionForShouldStopSupplier = singleRecursionForShouldStopSupplier;
    }

    public static Future<Void> call(Supplier<Future<Boolean>> singleRecursionForShouldStopSupplier) {
        //Queue<Promise<Boolean>> futures=new ConcurrentLinkedQueue<>();
        Promise<Void> promise = Promise.promise();
        new FutureUntil(singleRecursionForShouldStopSupplier).routine(promise);
        return promise.future();
    }

    public static void main(String[] args) {
        Keel.initializeVertx(new VertxOptions());
        AtomicInteger x = new AtomicInteger(0);
        FutureUntil.call(() -> {
                    int i = x.incrementAndGet();
                    Keel.outputLogger().info("i=" + i);
                    if (x.get() > 5) {
                        return Future.succeededFuture(true);
                    } else {
                        return Future.succeededFuture(false);
                    }
                })
                .onComplete(ar -> {
                    if (ar.succeeded()) {
                        Keel.outputLogger().info("done r: " + ar.result());
                    } else {
                        Keel.outputLogger().exception(ar.cause());
                    }
                });
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
