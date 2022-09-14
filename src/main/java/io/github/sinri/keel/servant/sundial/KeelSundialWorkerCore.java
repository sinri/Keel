package io.github.sinri.keel.servant.sundial;

import io.vertx.core.Future;

import java.util.Calendar;

/**
 * @since 2.8
 */
public interface KeelSundialWorkerCore {
    /**
     * @param calendar 触发的日历时间
     * @return 任务的结束future
     */
    Future<Void> work(Calendar calendar);
}
