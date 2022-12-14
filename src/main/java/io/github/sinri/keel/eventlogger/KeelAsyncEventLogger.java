package io.github.sinri.keel.eventlogger;

import io.github.sinri.keel.Keel;
import io.github.sinri.keel.core.logger.async.AsyncLoggingServiceAdapter;
import io.vertx.core.Future;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * @since 2.9.4 实验性设计
 */
public class KeelAsyncEventLogger implements KeelEventLogger {
    private final AsyncLoggingServiceAdapter<KeelEventLog> adapter;
    private final Queue<KeelEventLog> queue;
    private final int bufferSize = 1000;
    private boolean toClose = false;

    public KeelAsyncEventLogger(AsyncLoggingServiceAdapter<KeelEventLog> adapter) {
        this.queue = new ConcurrentLinkedQueue<>();
        this.adapter = adapter;

        start();
    }

    protected void start() {
        Keel.callFutureRepeat(routineResult -> {
            return Future.succeededFuture()
                    .compose(v -> {
                        return Keel.getVertx().executeBlocking(promise -> {
                            List<KeelEventLog> buffer = new ArrayList<>();
                            for (int i = 0; i < bufferSize; i++) {
                                KeelEventLog eventLog = this.queue.poll();
                                if (eventLog == null) {
                                    break;
                                }
                                buffer.add(eventLog);
                            }
                            if (buffer.isEmpty()) {
                                promise.fail("EMPTY");
                            } else {
                                getAdapter().dealWithLogs(buffer)
                                        .andThen(ar -> {
                                            promise.complete();
                                        });
                            }
                        });
                    })
                    .recover(throwable -> {
                        return Keel.callFutureSleep(1000L)
                                .compose(v -> {
                                    return Future.succeededFuture();
                                });
                    })
                    .compose(v -> {
                        if (toClose) routineResult.stop();
                        return Future.succeededFuture(null);
                    });
        });
    }

    public AsyncLoggingServiceAdapter<KeelEventLog> getAdapter() {
        return adapter;
    }

    @Override
    public void log(KeelEventLog eventLog) {
        this.queue.add(eventLog);
    }

    @Override
    public Future<Void> gracefullyClose() {
        toClose = true;
        return getAdapter().gracefullyClose();
    }
}
