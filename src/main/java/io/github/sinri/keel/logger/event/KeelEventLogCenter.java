package io.github.sinri.keel.logger.event;

import io.vertx.core.Future;
import io.vertx.core.Handler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @since 2.9.4 实验性设计
 */
public interface KeelEventLogCenter {

    void log(@Nonnull KeelEventLog eventLog);

    @Nullable
    Object processThrowable(@Nullable Throwable throwable);

    @Nonnull
    Future<Void> gracefullyClose();


    @Nonnull
    default KeelEventLogger createLogger(@Nonnull String presetTopic) {
        return createLogger(presetTopic, null);
    }

    @Nonnull
    default KeelEventLogger createLogger(@Nonnull String presetTopic, @Nullable Handler<KeelEventLog> editor) {
        return new KeelEventLoggerImpl(presetTopic, () -> this, editor);
    }

}
