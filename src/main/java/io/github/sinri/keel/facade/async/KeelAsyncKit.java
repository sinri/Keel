package io.github.sinri.keel.facade.async;

import io.github.sinri.keel.facade.Keel;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Promise;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * @since 3.0.0
 */
public interface KeelAsyncKit {
    /**
     * @since 2.9.3 callFutureRepeat
     * @since 3.0.0 repeatedlyCall
     */
    static Future<Void> repeatedlyCall(Function<FutureRepeat.RoutineResult, Future<Void>> routineResultFutureFunction) {
        return FutureRepeat.call(routineResultFutureFunction);
    }

    /**
     * @since 2.9 callFutureForEach
     * @since 3.0.0 iterativelyCall
     */
    static <T> Future<Void> iterativelyCall(Iterable<T> collection, Function<T, Future<Void>> itemProcessor) {
        return FutureForEach.call(collection, itemProcessor);
    }

    /**
     * @since 2.9 callFutureForRange
     * @since 3.0.0 stepwiseCall
     */
    static Future<Void> stepwiseCall(FutureForRange.Options options, Function<Integer, Future<Void>> handleFunction) {
        return FutureForRange.call(options, handleFunction);
    }

    /**
     * @since 2.9 callFutureForRange
     * @since 3.0.0 stepwiseCall
     */
    static Future<Void> stepwiseCall(int times, Function<Integer, Future<Void>> handleFunction) {
        return FutureForRange.call(times, handleFunction);
    }

    /**
     * @since 2.9 callFutureSleep
     * @since 3.0.0 sleep
     */
    static Future<Void> sleep(long t) {
        return FutureSleep.call(t);
    }

    static Future<Void> sleep(long t, Promise<Void> interrupter) {
        return FutureSleep.call(t, interrupter);
    }

    static <T> Future<Void> parallelForAllSuccess(Iterable<T> collection, Function<T, Future<Void>> itemProcessor) {
        return FutureForEachParallel.all(collection, itemProcessor);
    }

    static <T> Future<Void> parallelForAnySuccess(Iterable<T> collection, Function<T, Future<Void>> itemProcessor) {
        return FutureForEachParallel.any(collection, itemProcessor);
    }

    static <T> Future<Void> parallelForAllComplete(Iterable<T> collection, Function<T, Future<Void>> itemProcessor) {
        return FutureForEachParallel.join(collection, itemProcessor);
    }

    static <T, R> Future<FutureForEachParallel.ParallelResult> parallelForAllResult(Iterable<T> collection, Function<T, Future<R>> itemProcessor) {
        return FutureForEachParallel.call(collection, itemProcessor);
    }

    static Future<Void> exclusivelyCall(String lockName, Supplier<Future<Void>> exclusiveSupplier) {
        return Keel.getVertx().sharedData()
                .getLock(lockName)
                .compose(lock -> Future.succeededFuture()
                        .compose(v -> exclusiveSupplier.get())
                        .andThen(ar -> lock.release()));
    }

    /**
     * @param promiseHandler execute a regular job, even if it is blocking, handle method is decided by users.
     * @since 3.0.0
     */
    static void endless(Handler<Promise<Void>> promiseHandler) {
        Promise<Void> promise = Promise.promise();
        promiseHandler.handle(promise);
        promise.future()
                .andThen(ar -> Keel.getVertx()
                        .setTimer(1L, timerID -> endless(promiseHandler)));
    }

    /**
     * @param supplier
     * @since 3.0.1
     */
    static void endless(Supplier<Future<Void>> supplier) {
        KeelAsyncKit.repeatedlyCall(routineResult -> {
            return Future.succeededFuture()
                    .compose(v -> {
                        return supplier.get();
                    })
                    .eventually(v -> {
                        return Future.succeededFuture();
                    });
        });
    }

    /**
     * @since 3.0.1
     */
    static <R> Future<R> vertxizedCompletableFuture(CompletableFuture<R> completableFuture) {
        Promise<R> promise = Promise.promise();
        completableFuture.whenComplete((r, t) -> {
            if (t != null) {
                promise.fail(t);
            } else {
                promise.complete(r);
            }
        });
        return promise.future();
    }

    /**
     * @since 3.0.1
     */
    static <R> Future<R> vertxizedRawFuture(java.util.concurrent.Future<R> rawFuture, long sleepTime) {
        return KeelAsyncKit.repeatedlyCall(routineResult -> {
                    if (rawFuture.isDone() || rawFuture.isCancelled()) {
                        routineResult.stop();
                    }
                    return sleep(sleepTime);
                })
                .compose(slept -> {
                    try {
                        return Future.succeededFuture(rawFuture.get());
                    } catch (InterruptedException | ExecutionException e) {
                        return Future.failedFuture(e);
                    }
                });
    }

    @Deprecated
    static <R> Future<R> waitForCompletableFuture(CompletableFuture<R> completableFuture, long gapTime) {
        return KeelAsyncKit.repeatedlyCall(routineResult -> {
                    if (completableFuture.isDone() || completableFuture.isCancelled() || completableFuture.isCompletedExceptionally()) {
                        routineResult.stop();
                    }
                    return KeelAsyncKit.sleep(gapTime);
                })
                .compose(v -> {
                    if (completableFuture.isDone()) {
                        try {
                            R r = completableFuture.get();
                            return Future.succeededFuture(r);
                        } catch (ExecutionException | InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    } else {
                        throw new RuntimeException("not done");
                    }
                });
    }

//    static void main(String[] args) {
//        CompletableFuture<String> f = CompletableFuture.supplyAsync(new Supplier<String>() {
//            @Override
//            public String get() {
//                throw new RuntimeException("888");
//                //return "123";
//            }
//        });
//        f
//                .exceptionally(throwable -> {
//                    System.err.println("exceptionally:" + throwable);
//                    return "000";
//                })
//                .whenComplete((r, t) -> {
//                    System.out.println("r: " + r);
//                    System.err.println("t:" + t);
//                })
//        ;
//    }
}
