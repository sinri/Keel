package io.github.sinri.keel.logger.event.adapter;

import io.github.sinri.keel.logger.event.KeelEventLog;
import io.vertx.core.Future;
import io.vertx.core.Promise;

import java.util.List;

public interface KeelEventLoggerAdapter {

    /**
     * @since 2.9.4
     */
    void close(Promise<Void> promise);

    /**
     * @since 2.9.4
     */
    default Future<Void> gracefullyClose() {
        Promise<Void> voidPromise = Promise.promise();
        close(voidPromise);
        return voidPromise.future();
    }

    Future<Void> dealWithLogs(List<KeelEventLog> buffer);

    Object processThrowable(Throwable throwable);
}
