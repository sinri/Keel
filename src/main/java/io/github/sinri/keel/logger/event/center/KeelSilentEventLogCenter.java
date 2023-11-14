package io.github.sinri.keel.logger.event.center;

import io.github.sinri.keel.logger.KeelLogLevel;
import io.github.sinri.keel.logger.event.KeelEventLog;
import io.github.sinri.keel.logger.event.KeelEventLogCenter;
import io.vertx.core.Future;

import javax.annotation.Nonnull;

public class KeelSilentEventLogCenter implements KeelEventLogCenter {
    private final static KeelSilentEventLogCenter instance = new KeelSilentEventLogCenter();

    private KeelSilentEventLogCenter() {

    }

    public static KeelSilentEventLogCenter getInstance() {
        return instance;
    }

    @Nonnull
    @Override
    public KeelLogLevel getVisibleLevel() {
        return KeelLogLevel.SILENT;
    }

    @Override
    public void setVisibleLevel(@Nonnull KeelLogLevel level) {

    }

    @Override
    public void log(@Nonnull KeelEventLog eventLog) {

    }

    @Override
    public Object processThrowable(Throwable throwable) {
        return null;
    }

    @Override
    @Nonnull
    public Future<Void> gracefullyClose() {
        return Future.succeededFuture();
    }
}
