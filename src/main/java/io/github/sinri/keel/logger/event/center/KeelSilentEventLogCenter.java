package io.github.sinri.keel.logger.event.center;

import io.github.sinri.keel.facade.Keel;
import io.github.sinri.keel.logger.event.KeelEventLog;
import io.github.sinri.keel.logger.event.KeelEventLogCenter;
import io.vertx.core.Future;

public class KeelSilentEventLogCenter implements KeelEventLogCenter {
    private final static KeelSilentEventLogCenter instance = new KeelSilentEventLogCenter();

    private KeelSilentEventLogCenter() {

    }

    public static KeelSilentEventLogCenter getInstance() {
        return instance;
    }

    @Override
    public Keel getKeel() {
        return null;
    }

    @Override
    public void log(KeelEventLog eventLog) {

    }

    @Override
    public Object processThrowable(Throwable throwable) {
        return null;
    }

    @Override
    public Future<Void> gracefullyClose() {
        return Future.succeededFuture();
    }
}
