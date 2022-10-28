package io.github.sinri.keel.core.logger.async;

import io.vertx.core.Future;

import java.util.List;

public interface AsyncLoggingServiceAdapter<T> {
    Future<Void> dealWithLogs(List<T> buffer);

    void close();
}
