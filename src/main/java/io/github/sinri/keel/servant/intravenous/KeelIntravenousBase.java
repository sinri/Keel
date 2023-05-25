package io.github.sinri.keel.servant.intravenous;

import io.github.sinri.keel.facade.async.KeelAsyncKit;
import io.github.sinri.keel.verticles.KeelVerticleBase;
import io.vertx.core.Future;
import io.vertx.core.Promise;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @param <T>
 * @since 3.0.1 redesigned from the original KeelIntravenous
 */
abstract public class KeelIntravenousBase<T> extends KeelVerticleBase {
    private final Queue<T> queue;
    private final AtomicReference<Promise<Void>> interruptRef;

    public KeelIntravenousBase() {
        this.queue = new ConcurrentLinkedQueue<>();
        this.interruptRef = new AtomicReference<>();
    }

    abstract protected Future<Void> process(List<T> list);

    private int getConfiguredBatchSize() {
        var x = config().getInteger("batch_size", 1);
        if (x < 1) x = 1;
        return x;
    }

    public void add(T t) {
        queue.add(t);
        Promise<Void> currentInterrupt = getCurrentInterrupt();
        if (currentInterrupt != null) {
            currentInterrupt.tryComplete();
        }
    }

    private Promise<Void> getCurrentInterrupt() {
        return this.interruptRef.get();
    }

    @Override
    public void start() throws Exception {
        int configuredBatchSize = getConfiguredBatchSize();
        KeelAsyncKit.endless(promise -> {
            this.interruptRef.set(null);

            KeelAsyncKit.repeatedlyCall(routineResult -> {
                        List<T> buffer = new ArrayList<>();
                        while (true) {
                            T t = queue.poll();
                            if (t != null) {
                                buffer.add(t);
                                if (buffer.size() >= configuredBatchSize) {
                                    break;
                                }
                            } else {
                                break;
                            }
                        }
                        if (buffer.isEmpty()) {
                            routineResult.stop();
                            return Future.succeededFuture();
                        }

                        // got one job to do, no matter if done
                        return Future.succeededFuture()
                                .compose(v -> {
                                    return this.process(buffer);
                                })
                                .compose(v -> {
                                    return Future.succeededFuture();
                                }, throwable -> {
                                    return Future.succeededFuture();
                                });
                    })
                    .andThen(ar -> {
                        this.interruptRef.set(Promise.promise());

                        KeelAsyncKit.sleep(sleptTime(), getCurrentInterrupt())
                                .andThen(slept -> {
                                    promise.complete();
                                });
                    });
        });
    }

    protected long sleptTime() {
        return 1_000L;
    }
}
