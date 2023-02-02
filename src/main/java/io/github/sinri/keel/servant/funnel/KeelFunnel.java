package io.github.sinri.keel.servant.funnel;

import io.github.sinri.keel.facade.async.KeelAsyncKit;
import io.github.sinri.keel.verticles.KeelVerticleBase;
import io.vertx.core.Future;
import io.vertx.core.Promise;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

/**
 * @since 3.0.0
 */
public class KeelFunnel extends KeelVerticleBase {
    private final AtomicReference<Promise<Void>> interruptRef;
    private final Queue<Supplier<Future<Void>>> queue;

    public KeelFunnel() {
        this.queue = new ConcurrentLinkedQueue<>();
        this.interruptRef = new AtomicReference<>();
    }

    public void add(Supplier<Future<Void>> supplier) {
        queue.add(supplier);
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
        KeelAsyncKit.endless(promise -> {
            this.interruptRef.set(null);
            //System.out.println("ENDLESS "+System.currentTimeMillis());

            KeelAsyncKit.repeatedlyCall(routineResult -> {
                        Supplier<Future<Void>> supplier = queue.poll();
                        if (supplier == null) {
                            // no job to do
                            routineResult.stop();
                            return Future.succeededFuture();
                        }

                        // got one job to do, no matter if done
                        return Future.succeededFuture()
                                .compose(v -> {
                                    return supplier.get();
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
