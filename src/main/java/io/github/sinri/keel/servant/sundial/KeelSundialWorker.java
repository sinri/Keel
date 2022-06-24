package io.github.sinri.keel.servant.sundial;

import io.github.sinri.keel.core.logger.KeelLogger;
import io.vertx.core.Future;

import java.util.Calendar;
import java.util.function.Function;

/**
 * 定时任务
 *
 * @since 2.7
 */
public interface KeelSundialWorker {
    static KeelSundialWorker build(
            String name,
            String rawCronExpression,
            Function<Calendar, Future<Void>> workFunction,
            int parallelLimit,
            KeelLogger logger
    ) {
        return new KeelSundialWorkerImpl(
                name,
                rawCronExpression,
                workFunction,
                parallelLimit,
                logger
        );
    }

    /**
     * @return 定时任务的全局唯一识别命名
     */
    String getName();

    /**
     * 并行任务限制。
     * 例如，每分钟一次的任务，每次任务需运行2分钟时，如限制并行任务为1，则当前任务未结束时触发的任务将不被真正执行。
     *
     * @return 该定时任务的并行数量；小于等于0时表示可无穷多任务并行。
     */
    int getParallelLimit();

    /**
     * @return 解析完成的定时表达式
     */
    KeelCronExpression getParsedCronExpression();

    /**
     * @param calendar 当前日历时间
     * @return 是否当前时间符合触发条件
     */
    default boolean shouldRunNow(Calendar calendar) {
        return getParsedCronExpression().match(calendar);
    }

    KeelLogger getLogger();

    /**
     * @param calendar 触发的日历时间
     * @return 任务的结束future
     */
    Future<Void> work(Calendar calendar);
}