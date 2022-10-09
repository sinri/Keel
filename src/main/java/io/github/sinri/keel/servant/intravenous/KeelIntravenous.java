package io.github.sinri.keel.servant.intravenous;

import io.github.sinri.keel.Keel;
import io.github.sinri.keel.core.controlflow.FutureRecursion;
import io.github.sinri.keel.core.logger.KeelLogger;
import io.github.sinri.keel.verticles.KeelVerticle;
import io.vertx.core.Future;
import io.vertx.core.Handler;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

/**
 * 静脉滴注，用于小规模实时排队处理异步小任务（DROP），强行避免并发。
 *
 * @param <R> 小任务（DROP）的运行结论
 * @param <D> 小任务（DROP）
 * @since 2.7
 */
public class KeelIntravenous<R, D extends KeelIntravenousDrop> extends KeelVerticle {
    private final Queue<D> queue;
    private final KeelIntravenousConsumer<R, D> consumer;
    private final AtomicBoolean finishFlag = new AtomicBoolean(false);
    private final AtomicBoolean finishedFlag = new AtomicBoolean(false);
    private long restMS = 100L;
    private Function<KeelIntravenousTaskConclusion<R>, Future<Void>> taskConclusionHandler;
    private Handler<Void> finishHandler = null;

    public KeelIntravenous(KeelIntravenousConsumer<R, D> consumer) {
        this.queue = new ConcurrentLinkedQueue<>();
        this.consumer = consumer;
        setLogger(Keel.outputLogger("KeelIntravenous"));
        this.taskConclusionHandler = null;
    }

    @Override
    public KeelLogger getLogger() {
        return super.getLogger();
    }

    protected KeelIntravenousConsumer<R, D> getConsumer() {
        return consumer;
    }

    public KeelIntravenous<R, D> setTaskConclusionHandler(Function<KeelIntravenousTaskConclusion<R>, Future<Void>> taskConclusionHandler) {
        this.taskConclusionHandler = taskConclusionHandler;
        return this;
    }

    public long getRestMS() {
        return restMS;
    }

    public KeelIntravenous<R, D> setRestMS(long restMS) {
        this.restMS = restMS;
        return this;
    }

    @Override
    public void start() throws Exception {
        // daemon start
        routine();
    }

    private void routine() {
        FutureRecursion.call(
                        true,
                        b -> {
                            return Future.succeededFuture(!this.queue.isEmpty());
                        },
                        b -> {
                            D task = this.queue.poll();
                            if (task == null) return Future.succeededFuture(false);
                            getLogger().info("[READY  ] TASK " + task.getReference());
                            return Future.succeededFuture()
                                    .compose(v -> {
                                        try {
                                            return this.getConsumer().handle(task);
                                        } catch (Throwable throwable) {
                                            return Future.failedFuture(throwable);
                                        }
                                    })
                                    .compose(rConclusion -> {
                                        if (rConclusion.isDone()) {
                                            getLogger().info("[  DONE] TASK " + task.getReference() + " Feedback: " + rConclusion.getFeedback() + " Result: " + rConclusion.getResult());
                                        } else {
                                            getLogger().warning("[FAILED] TASK " + task.getReference() + " Feedback: " + rConclusion.getFeedback() + " Result: " + rConclusion.getResult());
                                        }
                                        if (this.taskConclusionHandler != null) {
                                            return this.taskConclusionHandler.apply(rConclusion);
                                        } else {
                                            return Future.succeededFuture();
                                        }
                                    })
                                    .compose(v -> {
                                        return Future.succeededFuture(!this.queue.isEmpty());
                                    });
                        }
                )
                .recover(throwable -> {
                    this.getLogger().exception("EXCEPTION INSIDE ROUTINE", throwable);
                    return Future.succeededFuture();
                })
                .eventually(over -> {
                    if (this.finishFlag.get()) {
                        this.finishedFlag.set(true);
                        if (this.finishHandler != null) {
                            this.finishHandler.handle(null);
                        }
                    } else {
                        Keel.getVertx().setTimer(getRestMS(), timerID -> {
                            routine();
                        });
                    }
                    return Future.succeededFuture();
                });

    }

    public void drip(D drop) {
        this.queue.add(drop);
    }

    /**
     * Tell KeelIntravenous instance to stop receive drops.
     */
    public void finish() {
        this.finishFlag.set(true);
    }

    /**
     * Tell KeelIntravenous instance to stop receive new drops,
     * and let finishHandler be called when cycle actually stopped.
     *
     * @param finishHandler the finish event handler
     */
    public void finish(Handler<Void> finishHandler) {
        this.finishFlag.set(true);
        this.finishHandler = finishHandler;
    }

    /**
     * @return true if KeelIntravenous is already finished.
     */
    public boolean isFinished() {
        return this.finishedFlag.get();
    }

    public int getDropCount() {
        return this.queue.size();
    }
}
