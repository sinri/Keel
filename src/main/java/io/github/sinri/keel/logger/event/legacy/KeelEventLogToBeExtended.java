package io.github.sinri.keel.logger.event.legacy;

import io.github.sinri.keel.logger.KeelLogLevel;

import javax.annotation.Nonnull;

/**
 * @since 3.1.10
 */
@Deprecated(since = "3.2.0", forRemoval = true)
abstract public class KeelEventLogToBeExtended extends KeelEventLogImpl {
    public KeelEventLogToBeExtended(@Nonnull KeelLogLevel level, @Nonnull String topic) {
        super(level, topic);
        prepare();
    }

    abstract protected void prepare();
}
