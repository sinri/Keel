package io.github.sinri.keel.logger.event;

import io.github.sinri.keel.facade.Keel;
import io.github.sinri.keel.logger.event.adapter.KeelEventLoggerAdapter;
import io.vertx.core.Future;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * @since 2.9.4 实验性设计
 */
public class KeelAsyncEventLogCenter implements KeelEventLogCenter {
    private final KeelEventLoggerAdapter adapter;
    private final Queue<KeelEventLog> queue;
    private final int bufferSize = 1000;
    private boolean toClose = false;
    private final Keel keel;

    public KeelAsyncEventLogCenter(Keel keel, KeelEventLoggerAdapter adapter) {
        this.keel = keel;
        this.queue = new ConcurrentLinkedQueue<>();
        this.adapter = adapter;

        start();
    }

    protected void start() {
        keel.repeatedlyCall(routineResult -> {
            return Future.succeededFuture()
                    .compose(v -> {
                        return keel.getVertx().executeBlocking(promise -> {
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
                        return keel.sleep(1000L)
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

    public KeelEventLoggerAdapter getAdapter() {
        return adapter;
    }

    @Override
    public Keel getKeel() {
        return this.keel;
    }

    @Override
    public void log(KeelEventLog eventLog) {
        this.queue.add(eventLog);
    }

    @Override
    public Object processThrowable(Throwable throwable) {
        return this.adapter.processThrowable(throwable);
    }

    @Override
    public Future<Void> gracefullyClose() {
        toClose = true;
        return getAdapter().gracefullyClose();
    }
}
