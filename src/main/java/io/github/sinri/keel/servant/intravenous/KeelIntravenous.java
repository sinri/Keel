package io.github.sinri.keel.servant.intravenous;

import io.vertx.core.Future;

import java.util.List;
import java.util.function.Function;

/**
 * @param <T>
 * @since 3.0.1 became a sugar
 */
public class KeelIntravenous<T> extends KeelIntravenousBase<T> {
    private final Function<List<T>, Future<Void>> processor;

    public KeelIntravenous(Function<List<T>, Future<Void>> processor) {
        super();
        this.processor = processor;
    }

    @Override
    protected Future<Void> process(List<T> list) {
        return processor.apply(list);
    }
}
