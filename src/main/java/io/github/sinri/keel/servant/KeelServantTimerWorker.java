package io.github.sinri.keel.servant;

import java.util.Calendar;
import java.util.concurrent.Semaphore;

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

    public void trigger(Calendar calendar) {
        if (!cronParser.match(calendar)) {
            return;
        }
        // lock it
        if (monopolizedSemaphore != null) {
            try {
                monopolizedSemaphore.acquire();
                // working
                work(calendar);
                monopolizedSemaphore.release();
            } catch (InterruptedException e) {
                // worker is monopolized now, just passover and wait for the next turn.
                e.printStackTrace();
            }
        } else {
            // working
            work(calendar);
        }
    }

    abstract protected void work(Calendar calendar);
}
