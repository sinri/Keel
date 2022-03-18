package io.github.sinri.keel.core.controlflow;

import io.vertx.core.Future;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

/**
 * @param <T> Type of elements in the source collection
 * @since 1.13
 * @deprecated
 */
public class FutureForV2<T> {
    private final T initValue;
    private final Function<T, Boolean> shouldContinueFunction;
    private final Function<T, T> pointerModifyFunction;

    /**
     * @param initValue              the initial value of pointer
     * @param shouldContinueFunction a function to check pointer, if false returned, stop
     * @param pointerModifyFunction  a function to modify pointer after a round
     */
    public FutureForV2(T initValue, Function<T, Boolean> shouldContinueFunction, Function<T, T> pointerModifyFunction) {
        this.initValue = initValue;
        this.shouldContinueFunction = shouldContinueFunction;
        this.pointerModifyFunction = pointerModifyFunction;
    }

    public Future<Void> run(Function<T, Future<Void>> handleFunction) {
        AtomicReference<Future<Void>> futureAtomicReference = new AtomicReference<>();
        futureAtomicReference.set(Future.succeededFuture(null));
        for (T t = initValue; shouldContinueFunction.apply(t); t = pointerModifyFunction.apply(t)) {
            T finalT = t;
            var f = futureAtomicReference.get()
                    .compose(previous -> handleFunction.apply(finalT));
            futureAtomicReference.set(f);
        }
        return futureAtomicReference.get();
    }
}
