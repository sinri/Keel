package io.github.sinri.keel.core.controlflow;

import io.vertx.core.Future;

import java.util.Collection;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

/**
 * @param <T> Type of elements in the source collection
 * @since 1.13
 */
public class FutureForEach<T> {
    private final Function<T, Future<Void>> asyncItemProcessFunction;

    public FutureForEach(Function<T, Future<Void>> itemProcessor) {
        this.asyncItemProcessFunction = itemProcessor;
    }

    public Future<Void> process(Collection<T> collection) {
        AtomicReference<Future<Void>> futureAtomicReference = new AtomicReference<>();
        futureAtomicReference.set(Future.succeededFuture());
        collection.forEach(t -> {
            var future = futureAtomicReference.get().compose(previousK -> asyncItemProcessFunction.apply(t));
            futureAtomicReference.set(future);
        });
        return futureAtomicReference.get();
    }
}
