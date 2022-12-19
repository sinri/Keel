package io.github.sinri.keel.facade.async;

import io.github.sinri.keel.facade.interfaces.KeelTraitForVertx;
import io.vertx.core.Future;

import java.util.function.Function;

/**
 * @since 3.0.0
 */
public interface KeelTraitForVertxAsync extends KeelTraitForVertx {
    /**
     * @since 2.9.3 callFutureRepeat
     * @since 3.0.0 repeatedlyCall
     */
    default Future<Void> repeatedlyCall(Function<FutureRepeat.RoutineResult, Future<Void>> routineResultFutureFunction) {
        return FutureRepeat.call(this, routineResultFutureFunction);
    }

    /**
     * @since 2.9 callFutureForEach
     * @since 3.0.0 iterativelyCall
     */
    default <T> Future<Void> iterativelyCall(Iterable<T> collection, Function<T, Future<Void>> itemProcessor) {
        return FutureForEach.call(this, collection, itemProcessor);
    }

    /**
     * @since 2.9 callFutureForRange
     * @since 3.0.0 stepwiseCall
     */
    default Future<Void> stepwiseCall(FutureForRange.Options options, Function<Integer, Future<Void>> handleFunction) {
        return FutureForRange.call(this, options, handleFunction);
    }

    /**
     * @since 2.9 callFutureForRange
     * @since 3.0.0 stepwiseCall
     */
    default Future<Void> stepwiseCall(int times, Function<Integer, Future<Void>> handleFunction) {
        return FutureForRange.call(this, times, handleFunction);
    }

    /**
     * @since 2.9 callFutureSleep
     * @since 3.0.0 sleep
     */
    default Future<Void> sleep(long t) {
        return FutureSleep.call(this, t);
    }

    default <T> Future<Void> parallelForAllSuccess(Iterable<T> collection, Function<T, Future<Void>> itemProcessor) {
        return FutureForEachParallel.all(collection, itemProcessor);
    }

    default <T> Future<Void> parallelForAnySuccess(Iterable<T> collection, Function<T, Future<Void>> itemProcessor) {
        return FutureForEachParallel.any(collection, itemProcessor);
    }

    default <T> Future<Void> parallelForAllComplete(Iterable<T> collection, Function<T, Future<Void>> itemProcessor) {
        return FutureForEachParallel.join(collection, itemProcessor);
    }

    default <T, R> Future<FutureForEachParallel.ParallelResult> parallelForAllResult(Iterable<T> collection, Function<T, Future<R>> itemProcessor) {
        return FutureForEachParallel.call(collection, itemProcessor);
    }
}
