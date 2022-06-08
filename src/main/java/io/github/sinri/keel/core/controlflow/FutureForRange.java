package io.github.sinri.keel.core.controlflow;

import io.vertx.core.Future;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

/**
 * 让同一个异步任务按照设定连续运行一定的次数。
 *
 * @since 1.13
 */
public class FutureForRange {
    private final Integer start;
    private final Integer end;
    private final Integer step;

    private FutureForRange(Integer start, Integer end, Integer step) {
        this.start = start;
        this.end = end;
        this.step = step;
    }

    private FutureForRange(Integer start, Integer end) {
        this.start = start;
        this.end = end;
        this.step = 1;
    }

    private FutureForRange(Integer end) {
        this.start = 0;
        this.end = end;
        this.step = 1;
    }

    @Deprecated(forRemoval = true)
    public static <T> Future<Void> quick(Integer times, Function<Integer, Future<Void>> handleFunction) {
        return call(times, handleFunction);
    }

    public static <T> Future<Void> call(Integer start, Integer end, Integer step, Function<Integer, Future<Void>> handleFunction) {
        return new FutureForRange(start, end, step).run(handleFunction);
    }

    public static <T> Future<Void> call(Integer start, Integer end, Function<Integer, Future<Void>> handleFunction) {
        return new FutureForRange(start, end).run(handleFunction);
    }

    public static <T> Future<Void> call(Integer times, Function<Integer, Future<Void>> handleFunction) {
        return new FutureForRange(times).run(handleFunction);
    }

    private Future<Void> run(Function<Integer, Future<Void>> handleFunction) {
        AtomicReference<Future<Void>> futureAtomicReference = new AtomicReference<>();
        futureAtomicReference.set(Future.succeededFuture(null));
        for (Integer t = start; t < end; t += step) {
            Integer finalT = t;
            var f = futureAtomicReference.get()
                    .compose(previous -> handleFunction.apply(finalT));
            futureAtomicReference.set(f);
        }
        return futureAtomicReference.get();
    }
}
