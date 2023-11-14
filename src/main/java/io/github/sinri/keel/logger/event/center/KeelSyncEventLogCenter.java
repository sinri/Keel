package io.github.sinri.keel.logger.event.center;

import io.github.sinri.keel.logger.KeelLogLevel;
import io.github.sinri.keel.logger.event.KeelEventLog;
import io.github.sinri.keel.logger.event.KeelEventLogCenter;
import io.github.sinri.keel.logger.event.adapter.KeelEventLoggerAdapter;
import io.vertx.core.Future;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class KeelSyncEventLogCenter implements KeelEventLogCenter {
    private final KeelEventLoggerAdapter adapter;
    private KeelLogLevel visibleLogLevel = KeelLogLevel.INFO;

    public KeelSyncEventLogCenter(KeelEventLoggerAdapter adapter) {
        this.adapter = adapter;
    }

    @Nonnull
    @Override
    public KeelLogLevel getVisibleLevel() {
        return this.visibleLogLevel;
    }

    @Override
    public void setVisibleLevel(@Nonnull KeelLogLevel level) {
        this.visibleLogLevel = level;
    }

    @Override
    public void log(@Nonnull KeelEventLog eventLog) {
        if (eventLog.level().isEnoughSeriousAs(getVisibleLevel())) {
            adapter.dealWithLogs(List.of(eventLog));
        }
    }

    @Override
    @Nullable
    public Object processThrowable(@Nullable Throwable throwable) {
        return this.adapter.processThrowable(throwable);
    }

    @Override
    @Nonnull
    public Future<Void> gracefullyClose() {
        return this.adapter.gracefullyClose();
    }
}
