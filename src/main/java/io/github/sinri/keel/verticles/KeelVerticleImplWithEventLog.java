package io.github.sinri.keel.verticles;

import io.github.sinri.keel.logger.event.KeelEventLog;
import io.github.sinri.keel.logger.event.KeelEventLogger;

import javax.annotation.Nonnull;

/**
 * @since 3.2.0
 */
abstract public class KeelVerticleImplWithEventLog extends KeelVerticleBase<KeelEventLog> {
    private @Nonnull KeelEventLogger logger;

    public KeelVerticleImplWithEventLog() {
        super();
        this.logger = KeelEventLogger.from(getIssueRecorder());
    }

    @Nonnull
    public KeelEventLogger getLogger() {
        return logger;
    }

    public void setLogger(@Nonnull KeelEventLogger eventLogger) {
        this.setIssueRecorder(eventLogger.getIssueRecorder());
        this.logger = eventLogger;
    }
}
