package io.github.sinri.keel.core.controlflow;

import io.vertx.core.Future;

import java.util.function.Function;

/**
 * Repeat one thing within Future Style alike using `while`.
 *
 * @param <T> class of the object to finally get
 * @since 1.7
 */
public class FutureWhile<T> {
    protected T lastValue;
    protected boolean deadInWhile;
    protected final Function<FutureWhile<T>, Boolean> whileStopJudgeFunction;
    protected final Function<T, Future<T>> cycleFunction;

    /**
     * Create a new FutureWhile instance!
     * First, think about the synchronized grammar:
     * ```
     * x=initValue;
     * while(!whileStopJudgeFunction(x)){
     *     x=cycleFunction(x)
     * }
     * // finally get x
     * ```
     * This class is a wrapper and toolkit for the `initValue`, `whileStopJudgeFunction` and `cycleFunction`,
     * and finally provide the final value of `x`.
     *
     * @param initValue
     * @param whileStopJudgeFunction
     * @param cycleFunction
     */
    public FutureWhile(
            T initValue,
            Function<FutureWhile<T>, Boolean> whileStopJudgeFunction,
            Function<T, Future<T>> cycleFunction
    ) {
        this.lastValue = initValue;
        this.deadInWhile = false;
        this.whileStopJudgeFunction = whileStopJudgeFunction;
        this.cycleFunction = cycleFunction;
    }

    public boolean isDeadInWhile() {
        return deadInWhile;
    }

    public T getLastValue() {
        return lastValue;
    }

    public Future<T> runInWhile() {
        Future<T> future = Future.succeededFuture(lastValue);
        while (!this.deadInWhile && !this.whileStopJudgeFunction.apply(this)) {
            future = future.compose(
                            x -> this.cycleFunction
                                    .apply(x)
                                    .compose(y -> {
                                        lastValue = y;
                                        return Future.succeededFuture(lastValue);
                                    })
                    )
                    .recover(throwable -> {
                        this.deadInWhile = true;
                        return Future.failedFuture(throwable);
                    });
        }
        return future;
    }
}
