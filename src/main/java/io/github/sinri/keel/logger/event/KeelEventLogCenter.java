package io.github.sinri.keel.logger.event;

import io.github.sinri.keel.logger.KeelLogLevel;
import io.vertx.core.Future;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Supplier;

/**
 * @since 2.9.4 实验性设计
 */
public interface KeelEventLogCenter {

    /**
     * @return Logs of this level or higher are visible.
     * @since 3.0.10
     */
    @Nonnull
    KeelLogLevel getVisibleLevel();

    /**
     * @param level Logs of this level or higher are visible.
     * @since 3.0.10
     */
    void setVisibleLevel(@Nonnull KeelLogLevel level);

    void log(@Nonnull KeelEventLog eventLog);

    @Nullable
    Object processThrowable(@Nullable Throwable throwable);

    @Nonnull
    Future<Void> gracefullyClose();


    @Nonnull
    default KeelEventLogger createLogger(@Nonnull String presetTopic) {
        return createLogger(presetTopic, null);
    }

    /**
     * @since 3.1.10
     */
    default KeelEventLogger createLogger(@Nonnull String presetTopic, @Nullable Supplier<? extends KeelEventLog> baseLogBuilder) {
        return new KeelEventLoggerImpl(presetTopic, () -> this, baseLogBuilder);
    }
}
