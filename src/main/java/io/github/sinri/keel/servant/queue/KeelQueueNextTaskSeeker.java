package io.github.sinri.keel.servant.queue;

import io.vertx.core.Future;

import java.util.function.Supplier;

/**
 * @since 2.7 moved here
 * @since 2.8 extends Supplier{Future{KeelQueueTask}}
 */
public interface KeelQueueNextTaskSeeker extends Supplier<Future<KeelQueueTask>> {

    /**
     * 找出一个task且其已完成lockTaskBeforeDeployment方法的调用
     *
     * @return Future为成功时，如内容为空，则说明已经找不到任务；如非空，则为准备好的任务。Future为失败时表示获取任务过程失败。
     * @since 2.8
     */
    @Override
    Future<KeelQueueTask> get();

    /**
     * @since 2.8 default to 10s
     */
    default long waitingMs() {
        return 1000 * 10;
    }
}
