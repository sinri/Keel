package io.github.sinri.keel.servant.sundial;

import io.github.sinri.keel.core.KeelCronExpression;
import io.github.sinri.keel.core.logger.KeelLogger;
import io.vertx.core.Future;

import java.util.Calendar;
import java.util.function.Function;

/**
 * 定时任务的基本实现
 *
 * @since 2.3 fix async wait bug
 * @since 2.7 rename from KeelServantTimerWorker
 */
@Deprecated(since = "2.9.3")
public class KeelSundialWorkerImpl implements KeelSundialWorker {
    private final String name;
    private final String rawCronExpression;
    private final KeelCronExpression cronExpression;
    private final KeelLogger logger;
    private final int parallelLimit;
    private final Function<Calendar, Future<Void>> workFunction;

    public KeelSundialWorkerImpl(
            String name,
            String rawCronExpression,
            Function<Calendar, Future<Void>> workFunction,
            int parallelLimit,
            KeelLogger logger
    ) {
        this.name = name;
        this.rawCronExpression = rawCronExpression;
        this.cronExpression = new KeelCronExpression(rawCronExpression);
        this.parallelLimit = parallelLimit;
        this.logger = logger;
        this.workFunction = workFunction;
    }

    public String getName() {
        return name;
    }

    public int getParallelLimit() {
        return parallelLimit;
    }

    public String getRawCronExpression() {
        return rawCronExpression;
    }

    public KeelCronExpression getParsedCronExpression() {
        return cronExpression;
    }

    public boolean shouldRunNow(Calendar calendar) {
        if (!cronExpression.match(calendar)) {
            getLogger().debug("CRON PARSER NOT MATCHES");
            return false;
        }
        return true;
    }

    @Override
    public KeelLogger getLogger() {
        return logger;
    }

    @Override
    public Future<Void> work(Calendar calendar) {
        return workFunction.apply(calendar);
    }
}
