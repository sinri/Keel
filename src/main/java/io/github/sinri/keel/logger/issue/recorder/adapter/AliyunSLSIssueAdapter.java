package io.github.sinri.keel.logger.issue.recorder.adapter;

import io.github.sinri.keel.core.TechnicalPreview;
import io.github.sinri.keel.facade.async.KeelAsyncKit;
import io.github.sinri.keel.logger.issue.record.KeelIssueRecord;
import io.github.sinri.keel.logger.issue.recorder.render.KeelIssueRecordRender;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @since 3.1.10
 */
@TechnicalPreview(since = "3.1.10")
abstract public class AliyunSLSIssueAdapter implements KeelIssueRecorderAdapter {

    private final Map<String, Queue<KeelIssueRecord<?>>> issueRecordQueueMap = new HashMap<>();

    public AliyunSLSIssueAdapter() {

    }

    @Override
    public void record(@Nonnull String topic, @Nullable KeelIssueRecord<?> issueRecord) {
        if (issueRecord != null) {
            this.fetchQueue(topic).add(issueRecord);
        }
    }

    @Nonnull
    private Queue<KeelIssueRecord<?>> fetchQueue(@Nonnull String topic) {
        return this.issueRecordQueueMap.computeIfAbsent(topic, x -> new ConcurrentLinkedQueue<>());
    }

    public final void start() {
        KeelAsyncKit.repeatedlyCall(routineResult -> {
            if (isStopped()) {
                routineResult.stop();
                return Future.succeededFuture();
            }

            Set<String> topics = this.issueRecordQueueMap.keySet();
            return KeelAsyncKit.iterativelyCall(topics, this::handleForTopic)
                    .compose(v -> {
                        AtomicLong total = new AtomicLong(0);
                        return KeelAsyncKit.iterativelyCall(topics, topic -> {
                                    total.addAndGet(this.issueRecordQueueMap.get(topic).size());
                                    return Future.succeededFuture();
                                })
                                .compose(vv -> {
                                    if (total.get() == 0) {
                                        return KeelAsyncKit.sleep(500L);
                                    } else {
                                        return Future.succeededFuture();
                                    }
                                });
                    });
        });
    }

    protected int bufferSize() {
        return 1000;
    }

    private Future<Void> handleForTopic(@Nonnull String topic) {
        Queue<KeelIssueRecord<?>> keelIssueRecords = this.issueRecordQueueMap.get(topic);
        List<KeelIssueRecord<?>> buffer = new ArrayList<>();
        while (true) {
            KeelIssueRecord<?> x = keelIssueRecords.poll();
            if (x == null) {
                break;
            }
            buffer.add(x);
            if (buffer.size() >= bufferSize()) {
                break;
            }
        }
        if (buffer.isEmpty()) return Future.succeededFuture();
        return handleIssueRecordsForTopic(topic, buffer);
    }

    abstract protected Future<Void> handleIssueRecordsForTopic(@Nonnull String topic, @Nonnull List<KeelIssueRecord<?>> buffer);

    @Override
    public KeelIssueRecordRender<JsonObject> issueRecordRender() {
        return KeelIssueRecordRender.renderForJsonObject();
    }
}
