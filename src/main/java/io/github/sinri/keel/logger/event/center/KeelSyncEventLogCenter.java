package io.github.sinri.keel.logger.event.center;

import io.github.sinri.keel.logger.event.KeelEventLog;
import io.github.sinri.keel.logger.event.KeelEventLogCenter;
import io.github.sinri.keel.logger.event.adapter.KeelEventLoggerAdapter;
import io.vertx.core.Future;

import java.util.List;

public class KeelSyncEventLogCenter implements KeelEventLogCenter {
    private final KeelEventLoggerAdapter adapter;

    public KeelSyncEventLogCenter(KeelEventLoggerAdapter adapter) {
        this.adapter = adapter;
    }

    @Override
    public void log(KeelEventLog eventLog) {
        adapter.dealWithLogs(List.of(eventLog));
    }

    @Override
    public Object processThrowable(Throwable throwable) {
        return this.adapter.processThrowable(throwable);
    }

    @Override
    public Future<Void> gracefullyClose() {
        return this.adapter.gracefullyClose();
    }
}
