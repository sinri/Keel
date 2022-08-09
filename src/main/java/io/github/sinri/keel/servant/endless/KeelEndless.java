package io.github.sinri.keel.servant.endless;

import io.github.sinri.keel.Keel;
import io.github.sinri.keel.verticles.KeelVerticle;
import io.vertx.core.Future;

import java.util.function.Supplier;

/**
 * 任务定期触发器，隔一段时间调用任务供应商获取任务执行。
 * Timer Triggered
 * - START
 * - Supplier.get()
 * - Set Next Timer
 * - END
 *
 * @since 2.7
 */
public class KeelEndless extends KeelVerticle {
    private final long restMS;
    private final Supplier<Future<Void>> supplier;

    /**
     * @param restMS   干完一组事情后休息的时间长度，单位为 千分之一秒
     * @param supplier
     */
    public KeelEndless(long restMS, Supplier<Future<Void>> supplier) {
        this.restMS = restMS;
        this.supplier = supplier;
    }

    /**
     * @since 2.8
     */
    public KeelEndless(Supplier<Future<Void>> supplier) {
        this.restMS = 30 * 1000L;// 30s
        this.supplier = supplier;
    }

    public Future<Void> routine() {
        // since 2.8 防止 inner exception 爆破
        try {
            return supplier.get();
        } catch (Throwable throwable) {
            return Future.failedFuture(throwable);
        }
    }

    public void routineWrapper() {
        Keel.getVertx().setTimer(restMS, timerID -> {
            routine().onComplete(done -> routineWrapper());
        });
    }

    @Override
    public void start() throws Exception {
        routineWrapper();
    }
}
