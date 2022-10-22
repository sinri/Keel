package io.github.sinri.keel.servant.injection;

import io.github.sinri.keel.Keel;
import io.github.sinri.keel.core.controlflow.FutureUntil1;
import io.github.sinri.keel.core.json.SimpleJsonifiableEntity;
import io.github.sinri.keel.core.logger.KeelLogger;
import io.github.sinri.keel.verticles.KeelVerticle;
import io.vertx.core.Future;

import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Supplier;

/**
 * @since 2.8
 */
public class KeelInjection extends KeelVerticle {

    private final Queue<InjectionDrop> queue = new ConcurrentLinkedQueue<>();
    private long interval = 1000L;


    public KeelInjection() {
        setLogger(Keel.outputLogger("KeelInjection"));
    }

    public KeelInjection(KeelLogger logger) {
        setLogger(logger);
    }

    @Override
    public void start() throws Exception {
        this.interval = Objects.requireNonNullElse(
                new SimpleJsonifiableEntity(config()).readLong("interval"),
                1000L
        );

        routine();
    }

    private void routine() {
        this.vertx.setTimer(this.interval, timerID -> {
            this.handleDrops()
                    .onComplete(v -> {
                        // 不管成功失败，都等下一轮
                        routine();
                    });
        });
    }

    /**
     * @return 要么执行完全部队列中的任务返回 success future，要么中途出错返回 failed future
     */
    private Future<Void> handleDrops() {
        return FutureUntil1.call(() -> {
                    InjectionDrop drop = queue.poll();
                    if (drop == null) {
                        // stop FutureUntil
                        return Future.succeededFuture(true);
                    }
                    // handle drop and continue FutureUntil
                    return Future.succeededFuture()
                            .compose(v -> drop.handle())
                            .compose(handled -> Future.succeededFuture(false));
                })
                .onComplete(asyncResult -> {
                    if (asyncResult.failed()) {
                        getLogger().exception("HANDLE DROPS REPORTED FAILURE", asyncResult.cause());
                    } else {
                        getLogger().debug("HANDLE DROPS DONE");
                    }
                });
    }

    /**
     * 1. 从队列中获取待执行的任务：没有任务 -> 2；有任务 -> 3。
     * 2. 扔回成功期货 false
     * 3. 执行任务，观测结果：没有异常 -> 4; 有异常 -> 5。
     * 4. 扔回成功期货 true
     * 5. 扔回失败期货
     */
    private Future<Boolean> handleDropAndCheckNext() {
        InjectionDrop drop = this.queue.poll();
        if (drop == null) {
            // stop!
            return Future.succeededFuture(false);
        }
        return Future.succeededFuture()
                .compose(v -> drop.handle())
                .compose(handled -> Future.succeededFuture(true));
    }

    public synchronized Future<Boolean> drip(Supplier<Future<Void>> dropSupplier) {
        InjectionDrop drop = dropSupplier::get;
        return Future.succeededFuture(this.queue.offer(drop));
    }

    public interface InjectionDrop {
        Future<Void> handle();
    }

    /**
     * @since 2.9
     */
    public int getDropCount() {
        return this.queue.size();
    }
}
