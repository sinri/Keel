package io.github.sinri.keel.core.logger.async;

import io.vertx.core.Future;
import io.vertx.core.Promise;

import java.util.List;

public interface AsyncLoggingServiceAdapter<T> {
    /**
     * Expected to run in blocked mode.
     *
     * @param buffer log items
     */
    Future<Void> dealWithLogs(List<T> buffer);

    @Deprecated(since = "2.9.4", forRemoval = true)
    default void close() {
        Promise<Void> voidPromise = Promise.promise();
        close(voidPromise);
    }

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
}
