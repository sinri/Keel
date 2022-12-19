package io.github.sinri.keel.logger.event.adapter;

import io.github.sinri.keel.facade.Keel;
import io.github.sinri.keel.helper.KeelHelpers;
import io.github.sinri.keel.logger.event.KeelEventLog;
import io.vertx.core.Future;
import io.vertx.core.Promise;

import java.util.List;

/**
 * 本类无需Keel实例。
 * 单例模式。
 */
public class OutputAdapter implements KeelEventLoggerAdapter {

    private static final OutputAdapter instance = new OutputAdapter();

    private OutputAdapter() {

    }

    public static OutputAdapter getInstance() {
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
        buffer.forEach(eventLog -> {
            System.out.println(eventLog.toString());
        });
        return Future.succeededFuture();
    }

    @Override
    public Object processThrowable(Throwable throwable) {
        return KeelHelpers.getInstance().stringHelper().renderThrowableChain(throwable);
    }
}
