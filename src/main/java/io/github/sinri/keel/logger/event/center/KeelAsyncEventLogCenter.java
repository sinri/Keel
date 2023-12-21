package io.github.sinri.keel.logger.event.center;

import io.github.sinri.keel.facade.async.KeelAsyncKit;
import io.github.sinri.keel.logger.KeelLogLevel;
import io.github.sinri.keel.logger.event.KeelEventLog;
import io.github.sinri.keel.logger.event.KeelEventLogCenter;
import io.github.sinri.keel.logger.event.adapter.KeelEventLoggerAdapter;
import io.vertx.core.Future;
import io.vertx.core.Promise;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * @since 2.9.4 实验性设计
 */
public class KeelAsyncEventLogCenter implements KeelEventLogCenter {
    private final @Nonnull KeelEventLoggerAdapter adapter;
    private final @Nonnull Queue<KeelEventLog> queue;
    private final int bufferSize = 1000;
    private final @Nonnull Promise<Void> closePromise;
    private boolean toClose = false;
    private KeelLogLevel visibleLogLevel = KeelLogLevel.INFO;

    public KeelAsyncEventLogCenter(@Nonnull KeelEventLoggerAdapter adapter) {
        this.queue = new ConcurrentLinkedQueue<>();
        this.adapter = adapter;
        this.closePromise = Promise.promise();

        start();
    }

    protected void start() {
        KeelAsyncKit.repeatedlyCall(routineResult -> {
                    return Future.succeededFuture()
                            .compose(v -> {
                                // done: Now executeBlocking with promise is deprecated, use KeelAsyncKit instead.
                                return KeelAsyncKit.executeBlocking(promise -> {
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
                                return KeelAsyncKit.sleep(1000L)
                                        .compose(v -> {
                                            return Future.succeededFuture();
                                        });
                            })
                            .compose(v -> {
                                if (toClose) {
                                    if (this.queue.isEmpty()) {
                                        routineResult.stop();
                                    }
                                }
                                return Future.succeededFuture(null);
                            });
                })
                .andThen(ended -> {
                    closePromise.complete();
                });
    }

    @Nonnull
    public KeelEventLoggerAdapter getAdapter() {
        return adapter;
    }

    @Nonnull
    @Override
    public KeelLogLevel getVisibleLevel() {
        return visibleLogLevel;
    }

    @Override
    public void setVisibleLevel(@Nonnull KeelLogLevel level) {
        this.visibleLogLevel = level;
    }

    @Override
    public void log(@Nonnull KeelEventLog eventLog) {
        if (toClose) {
            System.out.println("[warning] " + getClass().getName() + " TO CLOSE, LOG WOULD NOT BE RECEIVED");
            System.out.println(eventLog);
            return;
        }
        if (eventLog.level().isEnoughSeriousAs(getVisibleLevel())) {
            this.queue.add(eventLog);
        }
    }

    @Override
    public Object processThrowable(Throwable throwable) {
        return this.adapter.processThrowable(throwable);
    }

    @Override
    @Nonnull
    public Future<Void> gracefullyClose() {
        toClose = true;
        return this.closePromise.future().compose(v -> {
            return getAdapter().gracefullyClose();
        });
    }
}
