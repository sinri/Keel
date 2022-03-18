package io.github.sinri.keel.core.controlflow;

import io.vertx.core.Future;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

/**
 * @param <T> type of for-style pointer
 * @param <K>
 * @since 1.13
 * @deprecated 设计过于复杂了，还是别去丢人现眼了
 */
public class FutureForV1<T, K> {
    private final T initValue;
    private final Function<T, Boolean> shouldContinueFunction;
    private final Function<T, T> pointerModifyFunction;

    public FutureForV1(T initValue, Function<T, Boolean> shouldContinueFunction, Function<T, T> pointerModifyFunction) {
        this.initValue = initValue;
        this.shouldContinueFunction = shouldContinueFunction;
        this.pointerModifyFunction = pointerModifyFunction;
    }

    public Future<K> run(Function<T, K> targetObjectSeekFunction, Function<K, Future<K>> targetObjectHandleFunction) {
        AtomicReference<Future<K>> futureAtomicReference = new AtomicReference<>();
        futureAtomicReference.set(Future.succeededFuture(null));
        for (T t = initValue; shouldContinueFunction.apply(t); t = pointerModifyFunction.apply(t)) {
            var k = targetObjectSeekFunction.apply(t);
            var f = futureAtomicReference.get()
                    .compose(previousK -> targetObjectHandleFunction.apply(k));
            futureAtomicReference.set(f);
        }
        return futureAtomicReference.get();
    }
}
