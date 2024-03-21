package io.github.sinri.keel.servant.intravenous;

import io.vertx.core.Future;

import java.util.List;
import java.util.function.Function;

/**
 * @param <T> The type of drop
 * @since 3.0.1 became a sugar
 */
public class KeelIntravenous<T> extends KeelIntravenousBase<T> {
    private final Function<List<T>, Future<Void>> processor;


    public KeelIntravenous(Function<List<T>, Future<Void>> processor) {
        super();
        this.processor = processor;
    }

    public KeelIntravenous<T> setBatchSize(int batchSize) {
        if (batchSize < 1) batchSize = 1;
        this.batchSize = batchSize;
        return this;
    }

    public KeelIntravenous<T> setSleepTime(long sleepTime) {
        if (sleepTime < 1) sleepTime = 1;
        this.sleepTime = sleepTime;
        return this;
    }

    @Override
    protected Future<Void> process(List<T> list) {
        return processor.apply(list);
    }

}
