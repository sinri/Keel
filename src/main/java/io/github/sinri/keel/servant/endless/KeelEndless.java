package io.github.sinri.keel.servant.endless;

import io.github.sinri.keel.Keel;
import io.github.sinri.keel.verticles.KeelVerticle;
import io.vertx.core.Future;

import java.util.function.Supplier;

/**
 * 任务定期触发器，隔一段时间调用任务供应商获取任务执行。
 * 隔一段时间无条件跑一次。
 * Timer Triggered
 * - START
 * - Supplier.get()
 * - Set Next Timer
 * - END
 * <p>
 * 使用deploy开启，使用undeploy撤销。
 *
 * @since 2.7
 */
public class KeelEndless extends KeelVerticle {
    private final long restMS;
    private final Supplier<Future<Void>> supplier;

    /**
     * @param restMS   干完一组事情后休息的时间长度，单位为 千分之一秒
     * @param supplier 所谓的干完一组事情
     */
    public KeelEndless(long restMS, Supplier<Future<Void>> supplier) {
        this.restMS = restMS;
        this.supplier = supplier;
    }

    /**
     * @since 2.8
     */
    @Deprecated
    public KeelEndless(Supplier<Future<Void>> supplier) {
        this.restMS = 30 * 1000L;// 30s
        this.supplier = supplier;
    }

    private Future<Void> routine() {
        // since 2.8 防止 inner exception 爆破
        try {
            return supplier.get();
        } catch (Throwable throwable) {
            return Future.failedFuture(throwable);
        }
    }

    /**
     * @since 2.8 如果alive显示false，则不再策划下一波触发
     */
    private void routineWrapper() {
        Keel.getVertx()
                .setTimer(
                        restMS,
                        currentTimerID -> routine().onComplete(done -> routineWrapper())
                );
    }

    @Override
    public void start() throws Exception {
        routineWrapper();
    }
}
