package io.github.sinri.keel.logger.event.center;

import io.github.sinri.keel.logger.event.KeelEventLog;
import io.github.sinri.keel.logger.event.KeelEventLogCenter;
import io.github.sinri.keel.logger.event.adapter.KeelEventLoggerAdapter;
import io.vertx.core.Future;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class KeelSyncEventLogCenter implements KeelEventLogCenter {
    private final KeelEventLoggerAdapter adapter;

    public KeelSyncEventLogCenter(KeelEventLoggerAdapter adapter) {
        this.adapter = adapter;
    }

    @Override
    public void log(@Nonnull KeelEventLog eventLog) {
        adapter.dealWithLogs(List.of(eventLog));
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
