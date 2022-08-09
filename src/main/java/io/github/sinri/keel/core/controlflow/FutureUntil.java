package io.github.sinri.keel.core.controlflow;

import io.vertx.core.Future;

import java.util.function.Supplier;

/**
 * @since 2.8
 */
public class FutureUntil {
    //private final Function<Void, Future<Boolean>> singleRecursionFunction;
    private final Supplier<Future<Boolean>> singleRecursionForShouldStopSupplier;

//    private FutureUntil(Function<Void, Future<Boolean>> singleRecursionFunction) {
//        this.singleRecursionFunction = singleRecursionFunction;
//    }

    private FutureUntil(Supplier<Future<Boolean>> singleRecursionForShouldStopSupplier) {
        this.singleRecursionForShouldStopSupplier = singleRecursionForShouldStopSupplier;
    }

    public static Future<Void> call(Supplier<Future<Boolean>> singleRecursionForShouldStopSupplier) {
        return new FutureUntil(singleRecursionForShouldStopSupplier).start();
    }

    private Future<Void> start() {
        Future<Void> future = Future.succeededFuture();
        return recur(future)
                .compose(b -> {
                    return Future.succeededFuture();
                });
    }

    private Future<Boolean> recur(Future<Void> future) {
        return future.compose(v -> {
                    try {
                        return singleRecursionForShouldStopSupplier.get();
                    } catch (Throwable throwable) {
                        return Future.failedFuture(throwable);
                    }
                })
                .compose(outputShouldStop -> {
                    if (!outputShouldStop) {
                        // next
                        return recur(Future.succeededFuture());
                    } else {
                        // stop
                        return Future.succeededFuture();
                    }
                });
    }
}
