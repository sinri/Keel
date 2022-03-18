package io.github.sinri.keel.core.controlflow;

import io.vertx.core.Future;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

/**
 * @since 1.13
 */
public class FutureForRange {
    private final Integer start;
    private final Integer end;
    private final Integer step;

    public FutureForRange(Integer start, Integer end, Integer step) {
        this.start = start;
        this.end = end;
        this.step = step;
    }

    public FutureForRange(Integer start, Integer end) {
        this.start = start;
        this.end = end;
        this.step = 1;
    }

    public FutureForRange(Integer end) {
        this.start = 0;
        this.end = end;
        this.step = 1;
    }

    public Future<Void> run(Function<Integer, Future<Void>> handleFunction) {
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
