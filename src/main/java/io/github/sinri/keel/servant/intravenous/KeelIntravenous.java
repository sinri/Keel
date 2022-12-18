package io.github.sinri.keel.servant.intravenous;

import io.github.sinri.keel.verticles.KeelVerticleBase;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.MessageConsumer;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Function;

/**
 * 随时接收参数包，并周期性轮询以供批处理。
 * Like original Sisiodosi, but implemented batch processing by default.
 * <p>
 * 设计为单节点模式下使用，但可经扩展用于集群模式。
 *
 * @since 2.9
 */
public class KeelIntravenous<T> extends KeelVerticleBase {
    private final Queue<T> queue;

    private final Function<List<T>, Future<Void>> processor;
    private int batchSize;
    private long interval;

    private MessageConsumer<T> consumer;

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
                    .andThen(ar -> getKeel().setTimer(1L, x -> routine()));
        } else {
            getKeel().setTimer(interval, x -> routine());
        }
    }

    /**
     * @param address used in EventBus
     * @since 2.9
     */
    public void registerMessageConsumer(String address) {
        unregisterMessageConsumer(event -> consumer = getKeel().eventBus()
                .consumer(address, message -> drop(message.body())));
    }

    /**
     * @since 2.9
     */
    public void unregisterMessageConsumer(Handler<AsyncResult<Void>> completionHandler) {
        if (this.consumer != null) {
            this.consumer.unregister(completionHandler);
        } else {
            Future.succeededFuture((Void) null).onComplete(completionHandler);
        }
    }

    /**
     * @since 2.9
     */
    public void unregisterMessageConsumer() {
        unregisterMessageConsumer(event -> {

        });
    }

    @Override
    public void stop() throws Exception {
        super.stop();
        unregisterMessageConsumer();
    }

}
