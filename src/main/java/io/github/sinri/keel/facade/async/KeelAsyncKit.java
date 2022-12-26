package io.github.sinri.keel.facade.async;

import io.vertx.core.Future;

import java.util.function.Function;

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
}
