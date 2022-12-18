package io.github.sinri.keel.logger.event.adapter;

import io.github.sinri.keel.facade.Keel;
import io.github.sinri.keel.logger.event.KeelEventLog;
import io.vertx.core.Future;
import io.vertx.core.Promise;

import java.util.List;

/**
 * 本类无需Keel实例。
 * 单例模式。
 */
public final class SilentAdapter implements KeelEventLoggerAdapter {
    private static final SilentAdapter instance = new SilentAdapter();

    private SilentAdapter() {

    }

    public static SilentAdapter getInstance() {
        return instance;
    }

    @Override
    public Keel getKeel() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setKeel(Keel keel) {

    }

    @Override
    public void close(Promise<Void> promise) {
        promise.complete();
    }

    @Override
    public Future<Void> dealWithLogs(List<KeelEventLog> buffer) {
        return Future.succeededFuture();
    }

    @Override
    public Object processThrowable(Throwable throwable) {
        return null;
    }
}
