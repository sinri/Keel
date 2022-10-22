package io.github.sinri.keel.core.controlflow;

import io.github.sinri.keel.Keel;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.VertxOptions;

import java.util.concurrent.atomic.AtomicInteger;

public class FutureUntil2 {
    private final Handler<Promise<Boolean>> singleRecursionForShouldStopSupplier;

    private FutureUntil2(Handler<Promise<Boolean>> singleRecursionForShouldStopSupplier) {
        this.singleRecursionForShouldStopSupplier = singleRecursionForShouldStopSupplier;
    }

    public static Future<Boolean> call(Handler<Promise<Boolean>> singleRecursionForShouldStopSupplier) {
        //Queue<Promise<Boolean>> futures=new ConcurrentLinkedQueue<>();
        Promise<Boolean> promise = Promise.promise();
        new FutureUntil2(singleRecursionForShouldStopSupplier).routine(promise);
        return promise.future();
    }

    public static void main(String[] args) {
        Keel.initializeVertx(new VertxOptions());
        AtomicInteger x = new AtomicInteger(0);
        FutureUntil2.call(new Handler<Promise<Boolean>>() {
                    @Override
                    public void handle(Promise<Boolean> booleanPromise) {
                        int i = x.incrementAndGet();
                        Keel.outputLogger().info("i=" + i);
                        if (x.get() > 5) {
                            booleanPromise.complete(true);
                        } else {
                            booleanPromise.complete(false);
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

    private void routine(Promise<Boolean> finalPromise) {
        Promise<Boolean> currentPromise = Promise.promise();
        singleRecursionForShouldStopSupplier.handle(currentPromise);
        currentPromise.future()
                .andThen(shouldStopAR -> {
                    if (shouldStopAR.succeeded()) {
                        var shouldStop = shouldStopAR.result();
                        if (shouldStop) {
                            finalPromise.complete();
                        } else {
                            routine(finalPromise);
                        }
                    } else {
                        finalPromise.fail(shouldStopAR.cause());
                    }
                });
    }

}
