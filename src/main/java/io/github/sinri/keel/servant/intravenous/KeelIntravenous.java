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
import java.util.function.Function;

/**
 * 
 * @since 3.0.0
 */
public class KeelIntravenous<T> extends KeelVerticleBase {
    private final Queue<T> queue;
    private final AtomicReference<Promise<Void>> interruptRef;
    private final Function<List<T>, Future<Void>> processor;

    public KeelIntravenous(Function<List<T>, Future<Void>> processor) {
        this.queue = new ConcurrentLinkedQueue<>();
        this.interruptRef = new AtomicReference<>();
        this.processor = processor;
    }

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
            //System.out.println("ENDLESS "+System.currentTimeMillis());

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
                                    return this.processor.apply(buffer);
                                })
                                .compose(v -> {
                                    return Future.succeededFuture();
                                }, throwable -> {
                                    return Future.succeededFuture();
                                });
                    })
                    .andThen(ar -> {
                        this.interruptRef.set(Promise.promise());

                        KeelAsyncKit.sleep(60_000L, getCurrentInterrupt())
                                .andThen(slept -> {
                                    promise.complete();
                                });
                    });
        });
    }
}
