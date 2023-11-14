package io.github.sinri.keel.servant.funnel;

import io.github.sinri.keel.facade.async.KeelAsyncKit;
import io.github.sinri.keel.verticles.KeelVerticleBase;
import io.vertx.core.Future;
import io.vertx.core.Promise;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

/**
 * @since 3.0.0
 */
public class KeelFunnel extends KeelVerticleBase {
    /**
     * The interrupt, to stop sleeping when idle time ends (a new task comes).
     */
    private final AtomicReference<Promise<Void>> interruptRef;
    private final Queue<Supplier<Future<Void>>> queue;
    private final AtomicLong sleepTimeRef;

    public KeelFunnel() {
        this.sleepTimeRef = new AtomicLong(1_000L);
        this.queue = new ConcurrentLinkedQueue<>();
        this.interruptRef = new AtomicReference<>();
    }

    public void setSleepTime(long sleepTime) {
        if (sleepTime <= 1) {
            throw new IllegalArgumentException();
        }
        this.sleepTimeRef.set(sleepTime);
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
                                    //getLogger().debug("funnel done");
                                    return Future.succeededFuture();
                                }, throwable -> {
                                    getLogger().exception(throwable, "funnel task error");
                                    return Future.succeededFuture();
                                });
                    })
                    .andThen(ar -> {
                        this.interruptRef.set(Promise.promise());

                        KeelAsyncKit.sleep(this.sleepTimeRef.get(), getCurrentInterrupt())
                                .andThen(slept -> {
                                    promise.complete();
                                });
                    });
        });
    }

//    @Deprecated
//    public static void main(String[] args) {
//        Keel.initializeVertxStandalone(new VertxOptions());
//        KeelFunnel funnel = new KeelFunnel();
//        funnel.setLogger(KeelOutputEventLogCenter.getInstance().createLogger("FunnelMainTest"));
//        funnel.deployMe(new DeploymentOptions().setWorker(true)).compose(deploymentID -> {
//                    funnel.add(new Supplier<Future<Void>>() {
//                        @Override
//                        public Future<Void> get() {
//                            System.out.println("!!!");
//                            return Future.succeededFuture();
//                        }
//                    });
//                    return Future.succeededFuture();
//                }).compose(v -> {
//                    return KeelAsyncKit.sleep(3000L);
//                })
//                .eventually(v -> {
//                    return Keel.getVertx().close();
//                });
//    }
}
