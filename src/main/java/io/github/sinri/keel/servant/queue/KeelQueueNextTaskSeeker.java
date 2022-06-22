package io.github.sinri.keel.servant.queue;

import io.vertx.core.Future;

/**
 * @since 2.7 moved here
 */
public interface KeelQueueNextTaskSeeker {
    Future<Boolean> hasMore();

    /**
     * 找出一个task且其已完成lockTaskBeforeDeployment方法的调用
     */
    Future<KeelQueueTask> seek();

    long waitingMs();
}
