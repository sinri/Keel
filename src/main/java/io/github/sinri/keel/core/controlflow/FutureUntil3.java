package io.github.sinri.keel.core.controlflow;

import io.github.sinri.keel.Keel;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.VertxOptions;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

public class FutureUntil3 {
    private final Supplier<Future<Boolean>> singleRecursionForShouldStopSupplier;

    private FutureUntil3(Supplier<Future<Boolean>> singleRecursionForShouldStopSupplier) {
        this.singleRecursionForShouldStopSupplier = singleRecursionForShouldStopSupplier;
    }

    public static Future<Void> call(Supplier<Future<Boolean>> singleRecursionForShouldStopSupplier) {
        //Queue<Promise<Boolean>> futures=new ConcurrentLinkedQueue<>();
        Promise<Void> promise = Promise.promise();
        new FutureUntil3(singleRecursionForShouldStopSupplier).routine(promise);
        return promise.future();
    }

    public static void main(String[] args) {
        Keel.initializeVertx(new VertxOptions());
        AtomicInteger x = new AtomicInteger(0);
        FutureUntil3.call(new Supplier<Future<Boolean>>() {
                    @Override
                    public Future<Boolean> get() {
                        int i = x.incrementAndGet();
                        Keel.outputLogger().info("i=" + i);
                        if (x.get() > 5) {
                            return Future.succeededFuture(true);
                        } else {
                            return Future.succeededFuture(false);
                        }
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
