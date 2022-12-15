package io.github.sinri.keel.eventlogger;

import io.vertx.core.Future;

public class KeelSyncEventLogger implements KeelEventLogger {
    @Override
    public void log(KeelEventLog eventLog) {

    }

    @Override
    public Future<Void> gracefullyClose() {
        return null;
    }
}
