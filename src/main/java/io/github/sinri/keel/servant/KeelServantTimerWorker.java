package io.github.sinri.keel.servant;

import io.github.sinri.keel.core.logger.KeelLogger;
import io.vertx.core.Future;

import java.util.Calendar;
import java.util.concurrent.Semaphore;

/**
 * @since 2.3 fix async wait bug
 */
public abstract class KeelServantTimerWorker {
    private final String cronExpression;
    private final KeelCronParser cronParser;

    private Semaphore monopolizedSemaphore = null;

    public KeelServantTimerWorker(String cronExpression) {
        this.cronExpression = cronExpression;
        this.cronParser = new KeelCronParser(cronExpression);
    }

    public KeelServantTimerWorker declareMonopolized() {
        monopolizedSemaphore = new Semaphore(1);
        return this;
    }

    public String getCronExpression() {
        return cronExpression;
    }

    public KeelCronParser getCronParser() {
        return cronParser;
    }

    abstract protected KeelLogger getLogger();

    public void trigger(Calendar calendar) {
        if (!cronParser.match(calendar)) {
            getLogger().debug("CRON PARSER NOT MATCHES");
            return;
        }
        // lock it
        if (monopolizedSemaphore != null) {
            boolean acquired = monopolizedSemaphore.tryAcquire();
            if (acquired) {
                // working
                work(calendar)
                        .eventually(fin -> {
                            monopolizedSemaphore.release();
                            return Future.succeededFuture();
                        });
            } else {
                getLogger().warning("Monopolized Semaphore failed to acquire!");
            }
        } else {
            // working
            getLogger().debug("START");
            work(calendar);
        }
    }

    abstract protected Future<Void> work(Calendar calendar);
}
