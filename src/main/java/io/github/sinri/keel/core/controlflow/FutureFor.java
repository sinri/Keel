package io.github.sinri.keel.core.controlflow;

import io.vertx.core.Future;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

/**
 * @since 1.13
 */
public class FutureFor<T> {
    public Function<T, Future<Void>> handleFunction;

    public FutureFor(Function<T, Future<Void>> handleFunction) {
        this.handleFunction = handleFunction;
    }

    public Future<Void> run(
            T initValue,
            Function<T, Boolean> shouldContinueFunction,
            Function<T, T> pointerModifyFunction
    ) {
        AtomicReference<Future<Void>> futureAtomicReference = new AtomicReference<>();
        futureAtomicReference.set(Future.succeededFuture(null));
        for (T i = initValue; shouldContinueFunction.apply(i); i = pointerModifyFunction.apply(i)) {
            T finalT = i;
            var f = futureAtomicReference.get()
                    .compose(previous -> handleFunction.apply(finalT));
            futureAtomicReference.set(f);
        }
        return futureAtomicReference.get();
    }
}
