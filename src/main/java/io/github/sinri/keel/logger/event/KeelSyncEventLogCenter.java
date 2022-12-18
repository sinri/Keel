package io.github.sinri.keel.logger.event;

import io.github.sinri.keel.facade.Keel;
import io.github.sinri.keel.logger.event.adapter.KeelEventLoggerAdapter;
import io.vertx.core.Future;

import java.util.List;

public class KeelSyncEventLogCenter implements KeelEventLogCenter {
    private final Keel keel;
    private final KeelEventLoggerAdapter adapter;

    public KeelSyncEventLogCenter(Keel keel, KeelEventLoggerAdapter adapter) {
        this.keel = keel;
        this.adapter = adapter;
    }


    @Override
    public Keel getKeel() {
        return this.keel;
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
