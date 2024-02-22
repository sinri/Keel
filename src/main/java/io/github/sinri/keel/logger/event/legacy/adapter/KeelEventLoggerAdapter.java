package io.github.sinri.keel.logger.event.legacy.adapter;

import io.github.sinri.keel.logger.event.legacy.KeelEventLog;
import io.vertx.core.Future;
import io.vertx.core.Promise;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public interface KeelEventLoggerAdapter {

    /**
     * @since 2.9.4
     */
    void close(@Nonnull Promise<Void> promise);

    /**
     * @since 2.9.4
     */
    @Nonnull
    default Future<Void> gracefullyClose() {
        Promise<Void> voidPromise = Promise.promise();
        close(voidPromise);
        return voidPromise.future();
    }

    @Nonnull
    Future<Void> dealWithLogs(@Nonnull List<KeelEventLog> buffer);

    @Nullable
    Object processThrowable(@Nullable Throwable throwable);
}
