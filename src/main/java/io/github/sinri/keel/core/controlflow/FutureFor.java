package io.github.sinri.keel.core.controlflow;

import io.vertx.core.Future;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

/**
 * @since 1.13
 */
public class FutureFor<T> {
    private final Function<T, Future<Void>> handleFunction;

    private FutureFor(Function<T, Future<Void>> handleFunction) {
        this.handleFunction = handleFunction;
    }

    public static <T> Future<Void> call(
            T initValue,
            Function<T, Boolean> shouldContinueFunction,
            Function<T, T> pointerModifyFunction,
            Function<T, Future<Void>> handleFunction
    ) {
        return new FutureFor<T>(
                handleFunction
        ).run(
                initValue,
                shouldContinueFunction,
                pointerModifyFunction
        );
    }

    private Future<Void> run(
            T initValue,
            Function<T, Boolean> shouldContinueFunction,
            Function<T, T> pointerModifyFunction
    ) {
        AtomicReference<Future<Void>> futureAtomicReference = new AtomicReference<>();
        futureAtomicReference.set(Future.succeededFuture(null));
        for (T i = initValue; shouldContinueFunction.apply(i); i = pointerModifyFunction.apply(i)) {
            T finalT = i;
            var f = futureAtomicReference.get()
                    .compose(previous -> handleFunction.apply(finalT))
                    .onFailure(throwable -> {
                        throw new RuntimeException("FutureFor::run failed in routine", throwable);
                    });
            futureAtomicReference.set(f);
        }
        return futureAtomicReference.get();
    }
}
