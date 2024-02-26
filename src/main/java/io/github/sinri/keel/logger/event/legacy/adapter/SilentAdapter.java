package io.github.sinri.keel.logger.event.legacy.adapter;

import io.github.sinri.keel.logger.event.legacy.KeelEventLog;
import io.vertx.core.Future;
import io.vertx.core.Promise;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

/**
 * 本类无需Keel实例。
 * 单例模式。
 */
@Deprecated(since = "3.2.0", forRemoval = true)
public final class SilentAdapter implements KeelEventLoggerAdapter {
    private static final SilentAdapter instance = new SilentAdapter();

    private SilentAdapter() {

    }

    public static SilentAdapter getInstance() {
        return instance;
    }

    @Override
    public void close(@Nonnull Promise<Void> promise) {
        promise.complete();
    }

    @Override
    @Nonnull
    public Future<Void> dealWithLogs(@Nonnull List<KeelEventLog> buffer) {
        return Future.succeededFuture();
    }

    @Override
    @Nullable
    public Object processThrowable(@Nullable Throwable throwable) {
        return null;
    }
}
