package io.github.sinri.keel.core.controlflow;

import io.vertx.core.Future;

import java.util.concurrent.atomic.AtomicInteger;
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

    public static <T> Future<Void> call(Integer start, Integer end, Integer step, Function<Integer, Future<Void>> handleFunction) {
        return new FutureForRange(start, end, step).run(handleFunction);
    }

    public static <T> Future<Void> call(Integer start, Integer end, Function<Integer, Future<Void>> handleFunction) {
        return new FutureForRange(start, end).run(handleFunction);
    }

    public static <T> Future<Void> call(Integer times, Function<Integer, Future<Void>> handleFunction) {
        return new FutureForRange(times).run(handleFunction);
    }

    /**
     * @since 2.8.1 use FutureUntil to avoid Thread Blocking Issue.
     */
    private Future<Void> run(Function<Integer, Future<Void>> handleFunction) {
        AtomicInteger indexRef = new AtomicInteger(start);
        return FutureUntil3.call(() -> {
            if (indexRef.get() < end) {
                return Future.succeededFuture()
                        .compose(v -> {
                            return handleFunction.apply(indexRef.get());
                        })
                        .compose(v -> {
                            indexRef.addAndGet(step);
                            return Future.succeededFuture(false);
                        });
            } else {
                return Future.succeededFuture(true);
            }
        });

//        AtomicReference<Future<Void>> futureAtomicReference = new AtomicReference<>();
//        futureAtomicReference.set(Future.succeededFuture(null));
//        for (Integer t = start; t < end; t += step) {
//            Integer finalT = t;
//            var f = futureAtomicReference.get()
//                    .compose(previous -> {
//                        try {
//                            return handleFunction.apply(finalT);
//                        } catch (Throwable throwable) {
//                            return Future.failedFuture(throwable);
//                        }
//                    });
//            futureAtomicReference.set(f);
//        }
//        return futureAtomicReference.get();
    }
}
