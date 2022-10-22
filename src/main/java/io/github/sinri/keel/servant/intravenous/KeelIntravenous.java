package io.github.sinri.keel.servant.intravenous;

import io.github.sinri.keel.Keel;
import io.github.sinri.keel.verticles.KeelVerticleInterface;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Function;

/**
 * @since 2.9
 */
public class KeelIntravenous<T> extends AbstractVerticle implements KeelVerticleInterface {
    private final Queue<T> queue;

    private final Function<T, Future<Void>> dripProcessor;
    private long restTimeout;

    public KeelIntravenous(Function<T, Future<Void>> dripProcessor) {
        this.queue = new ConcurrentLinkedQueue<>();
        this.dripProcessor = dripProcessor;
        this.restTimeout = 100L;
    }

    public KeelIntravenous<T> setRestTimeout(long restTimeout) {
        this.restTimeout = restTimeout;
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
            Future.succeededFuture()
                    .compose(v -> this.dripProcessor.apply(this.queue.poll()))
                    .andThen(ar -> Keel.getVertx().setTimer(1L, x -> routine()));
        } else {
            Keel.getVertx().setTimer(restTimeout, x -> routine());
        }
    }
}
