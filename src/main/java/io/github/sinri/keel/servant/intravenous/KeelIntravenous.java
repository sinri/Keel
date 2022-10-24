package io.github.sinri.keel.servant.intravenous;

import io.github.sinri.keel.Keel;
import io.github.sinri.keel.verticles.KeelVerticleInterface;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Function;

/**
 * 随时接收参数包，并周期性轮询以供批处理。
 * Like original Sisiodosi, but implemented batch processing by default.
 *
 * @since 2.9
 */
public class KeelIntravenous<T> extends AbstractVerticle implements KeelVerticleInterface {
    private final Queue<T> queue;

    private final Function<List<T>, Future<Void>> processor;
    private int batchSize;
    private long interval;

    public KeelIntravenous(Function<List<T>, Future<Void>> processor) {
        this.queue = new ConcurrentLinkedQueue<>();
        this.processor = processor;
        this.interval = 100L;
        this.batchSize = 1;
    }

    public KeelIntravenous<T> setInterval(long interval) {
        this.interval = interval;
        return this;
    }

    public KeelIntravenous<T> setBatchSize(int batchSize) {
        this.batchSize = batchSize;
        return this;
    }

    public void drop(T drip) {
        queue.add(drip);
    }

    @Override
    public void start() throws Exception {
        super.start();
        routine();
    }

    private void routine() {
        if (this.queue.size() > 0) {
            List<T> l = new ArrayList<>();
            int i = 0;
            while (!this.queue.isEmpty() && i < batchSize) {
                l.add(this.queue.poll());
            }
            Future.succeededFuture()
                    .compose(v -> this.processor.apply(l))
                    .andThen(ar -> Keel.getVertx().setTimer(1L, x -> routine()));
        } else {
            Keel.getVertx().setTimer(interval, x -> routine());
        }
    }
}
