package io.github.sinri.keel.facade.async;

import io.github.sinri.keel.verticles.KeelVerticleBase;
import io.vertx.core.*;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;
import java.util.function.Supplier;

import static io.github.sinri.keel.facade.KeelInstance.Keel;

/**
 * @since 3.0.0
 * @since 3.0.8 changed a lot.
 */
public interface KeelAsyncKit {
    /**
     * @since 2.9.3 callFutureRepeat
     * @since 3.0.0 repeatedlyCall
     */
    static Future<Void> repeatedlyCall(@Nonnull Function<FutureRepeat.RoutineResult, Future<Void>> routineResultFutureFunction) {
        return FutureRepeat.call(routineResultFutureFunction);
    }

    /**
     * @since 2.9 callFutureForEach
     * @since 3.0.0 iterativelyCall
     */
    static <T> Future<Void> iterativelyCall(@Nonnull Iterable<T> collection, @Nonnull Function<T, Future<Void>> itemProcessor) {
        return FutureForEach.call(collection, itemProcessor);
    }

    /**
     * @since 3.0.13
     */
    static <T> Future<Void> iterativelyCall(@Nonnull Iterator<T> iterator, @Nonnull Function<T, Future<Void>> itemProcessor) {
        return repeatedlyCall(routineResult -> {
            if (iterator.hasNext()) {
                T t = iterator.next();
                return itemProcessor.apply(t);
            } else {
                routineResult.stop();
                return Future.succeededFuture();
            }
        });
    }

    /**
     * @since 3.0.13
     */
    static <T> Future<Void> iterativelyBatchCall(@Nonnull Iterator<T> iterator, @Nonnull Function<List<T>, Future<Void>> itemsProcessor, int batchSize) {
        if (batchSize < 1) throw new IllegalArgumentException("BATCH SIZE IS AT LEAST 1.");
        return repeatedlyCall(routineResult -> {
            List<T> buffer = new ArrayList<>();
            boolean hasNext = iterator.hasNext();
            while (hasNext) {
                T t = iterator.next();
                buffer.add(t);
                hasNext = iterator.hasNext();

                if (!hasNext) {
                    routineResult.stop();
                }

                if (buffer.size() >= batchSize) {
                    break;
                }
            }
            if (!buffer.isEmpty()) {
                return itemsProcessor.apply(buffer);
            } else {
                return Future.succeededFuture();
            }
        });
    }

    /**
     * @since 2.9 callFutureForRange
     * @since 3.0.0 stepwiseCall
     */
    static Future<Void> stepwiseCall(@Nonnull FutureForRange.Options options, @Nonnull Function<Integer, Future<Void>> handleFunction) {
        return FutureForRange.call(options, handleFunction);
    }

    /**
     * @since 2.9 callFutureForRange
     * @since 3.0.0 stepwiseCall
     */
    static Future<Void> stepwiseCall(int times, @Nonnull Function<Integer, Future<Void>> handleFunction) {
        return FutureForRange.call(times, handleFunction);
    }

    /**
     * @since 2.9 callFutureSleep
     * @since 3.0.0 sleep
     */
    static Future<Void> sleep(long t) {
        return FutureSleep.call(t);
    }

    static Future<Void> sleep(long t, @Nullable Promise<Void> interrupter) {
        return FutureSleep.call(t, interrupter);
    }

    static <T> Future<Void> parallelForAllSuccess(@Nonnull Iterable<T> collection, @Nonnull Function<T, Future<Void>> itemProcessor) {
        return FutureForEachParallel.all(collection, itemProcessor);
    }

    static <T> Future<Void> parallelForAnySuccess(@Nonnull Iterable<T> collection, @Nonnull Function<T, Future<Void>> itemProcessor) {
        return FutureForEachParallel.any(collection, itemProcessor);
    }

    static <T> Future<Void> parallelForAllComplete(@Nonnull Iterable<T> collection, @Nonnull Function<T, Future<Void>> itemProcessor) {
        return FutureForEachParallel.join(collection, itemProcessor);
    }

    static <T, R> Future<FutureForEachParallel.ParallelResult<R>> parallelForAllResult(@Nonnull Iterable<T> collection, @Nonnull Function<T, Future<R>> itemProcessor) {
        return FutureForEachParallel.call(collection, itemProcessor);
    }

    /**
     * @since 3.1.0
     */
    static <T> Future<T> exclusivelyCall(@Nonnull String lockName, long waitTimeForLock, @Nonnull Supplier<Future<T>> exclusiveSupplier) {
        return Keel.getVertx().sharedData()
                .getLockWithTimeout(lockName, waitTimeForLock)
                .compose(lock -> Future.succeededFuture()
                        .compose(v -> exclusiveSupplier.get())
                        .andThen(ar -> lock.release())
                );
    }

    /**
     * @since 3.0.11 not only Void!
     */
    static <T> Future<T> exclusivelyCall(@Nonnull String lockName, @Nonnull Supplier<Future<T>> exclusiveSupplier) {
        return exclusivelyCall(lockName, 1000L, exclusiveSupplier);
    }

    /**
     * @param promiseHandler execute a regular job, even if it is blocking, handle method is decided by users.
     * @since 3.0.0
     */
    static void endless(@Nonnull Handler<Promise<Void>> promiseHandler) {
        Promise<Void> promise = Promise.promise();
        promiseHandler.handle(promise);
        promise.future()
                .andThen(ar -> Keel.getVertx()
                        .setTimer(1L, timerID -> endless(promiseHandler)));
    }

    /**
     * @param supplier An async job handler, results a Void Future.
     * @since 3.0.1
     */
    static void endless(@Nonnull Supplier<Future<Void>> supplier) {
        KeelAsyncKit.repeatedlyCall(routineResult -> Future.succeededFuture()
                .compose(v -> supplier.get())
                .eventually(() -> Future.succeededFuture()));
    }

    /**
     * @since 3.0.1
     */
    static <R> Future<R> vertxizedCompletableFuture(@Nonnull CompletableFuture<R> completableFuture) {
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
    static <R> Future<R> vertxizedRawFuture(@Nonnull java.util.concurrent.Future<R> rawFuture, long sleepTime) {
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

    /**
     * @since 3.0.10 Technical Preview: instead of Vertx::executeBlocking(Handler&lt;Promise&lt;T&gt;&gt; blockingCodeHandler)
     * @since 3.0.18 Finished Technical Preview.
     */
    @Nonnull
    static <T> Future<T> executeBlocking(@Nonnull Handler<Promise<T>> blockingCodeHandler) {
        Promise<T> promise = Promise.promise();
        KeelVerticleBase verticle = new KeelVerticleBase() {
            @Override
            public void start() throws Exception {
                blockingCodeHandler.handle(promise);
                promise.future().onComplete(ar -> this.undeployMe());
            }
        };
        return verticle.deployMe(new DeploymentOptions()
                                .setThreadingModel(ThreadingModel.WORKER)
                        //.setWorker(true)
                )
                .compose(deploymentId -> promise.future());
    }
}
