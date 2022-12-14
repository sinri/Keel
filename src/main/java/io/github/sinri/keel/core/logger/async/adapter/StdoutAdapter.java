package io.github.sinri.keel.core.logger.async.adapter;

import io.github.sinri.keel.core.logger.async.AsyncLoggingServiceAdapter;
import io.vertx.core.Future;
import io.vertx.core.Promise;

import java.util.List;

/**
 * @param <T>
 * @since 2.9.3
 */
public class StdoutAdapter<T> implements AsyncLoggingServiceAdapter<T> {
    @Override
    public Future<Void> dealWithLogs(List<T> buffer) {
        buffer.forEach(t -> System.out.println(t.toString()));
        return Future.succeededFuture();
    }

    @Override
    public void close() {
        // do nothing
    }

    @Override
    public void close(Promise<Void> promise) {
        close();
        promise.complete();
    }
}
