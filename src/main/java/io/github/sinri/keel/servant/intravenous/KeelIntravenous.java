package io.github.sinri.keel.servant.intravenous;

import io.github.sinri.keel.Keel;
import io.github.sinri.keel.core.controlflow.FutureRecursion;
import io.github.sinri.keel.verticles.KeelVerticle;
import io.vertx.core.Future;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Function;

/**
 * 静脉滴注，用于小规模实时排队处理异步小任务（DROP），强行避免并发。
 *
 * @param <R> 小任务（DROP）的运行结论
 * @param <T> 小任务（DROP）
 * @since 2.7
 */
public class KeelIntravenous<R, T extends KeelIntravenousDrop> extends KeelVerticle {
    private final Queue<T> queue;
    private final KeelIntravenousConsumer<R, T> consumer;
    private long restMS = 100L;
    private Function<KeelIntravenousTaskConclusion<R>, Future<Void>> taskConclusionHandler;

    public KeelIntravenous(KeelIntravenousConsumer<R, T> consumer) {
        this.queue = new ConcurrentLinkedQueue<>();
        this.consumer = consumer;
        setLogger(Keel.outputLogger("KeelIntravenous"));
        this.taskConclusionHandler = null;
    }

    protected KeelIntravenousConsumer<R, T> getConsumer() {
        return consumer;
    }

    public KeelIntravenous<R, T> setTaskConclusionHandler(Function<KeelIntravenousTaskConclusion<R>, Future<Void>> taskConclusionHandler) {
        this.taskConclusionHandler = taskConclusionHandler;
        return this;
    }

    public long getRestMS() {
        return restMS;
    }

    public KeelIntravenous<R, T> setRestMS(long restMS) {
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
                            T task = this.queue.poll();
                            if (task == null) return Future.succeededFuture(false);
                            getLogger().info("[READY  ] TASK " + task.getReference());
                            return this.getConsumer()
                                    .handle(task)
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
                    Keel.getVertx().setTimer(getRestMS(), timerID -> {
                        routine();
                    });
                    return Future.succeededFuture();
                });

    }

    public void addNewTask(T task) {
        this.queue.add(task);
    }
}
