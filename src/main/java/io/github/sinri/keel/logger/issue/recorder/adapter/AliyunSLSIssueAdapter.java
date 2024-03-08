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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicLong;

import static io.github.sinri.keel.facade.KeelInstance.Keel;

/**
 * @since 3.1.10
 */
@TechnicalPreview(since = "3.1.10")
abstract public class AliyunSLSIssueAdapter implements KeelIssueRecorderAdapter {

    private final Map<String, Queue<KeelIssueRecord<?>>> issueRecordQueueMap = new ConcurrentHashMap<>();

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
                        Keel.getLogger().warning("AliyunSLSIssueAdapter routine to stop");
                        routineResult.stop();
                        return Future.succeededFuture();
                    }

                    Set<String> topics = Collections.unmodifiableSet(this.issueRecordQueueMap.keySet());
                    //Keel.getLogger().warning("AliyunSLSIssueAdapter routine unmodifiableSet of topics got");
                    return KeelAsyncKit.iterativelyCall(topics, this::handleForTopic)
                            .compose(v -> {
                                //Keel.getLogger().warning("AliyunSLSIssueAdapter routine unmodifiableSet of topics all handled");
                                AtomicLong total = new AtomicLong(0);
                                return KeelAsyncKit.iterativelyCall(topics, topic -> {
                                            total.addAndGet(this.issueRecordQueueMap.get(topic).size());
                                            return Future.succeededFuture();
                                        })
                                        .compose(vv -> {
                                            //Keel.getLogger().warning("AliyunSLSIssueAdapter routine counted");
                                            if (total.get() == 0) {
                                                return KeelAsyncKit.sleep(500L);
                                            } else {
                                                return Future.succeededFuture();
                                            }
                                        });
                            });
                })
                .onFailure(throwable -> {
                    Keel.getLogger().exception(throwable, "AliyunSLSIssueAdapter routine exception");
                });
    }

    protected int bufferSize() {
        return 1000;
    }

    private Future<Void> handleForTopic(@Nonnull final String topic) {
        //Keel.getLogger().warning("AliyunSLSIssueAdapter handleForTopic start for TOPIC "+topic);
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
        //Keel.getLogger().warning("AliyunSLSIssueAdapter handleForTopic concluded for TOPIC "+topic);
        return handleIssueRecordsForTopic(topic, buffer);
    }

    abstract protected Future<Void> handleIssueRecordsForTopic(@Nonnull final String topic, @Nonnull final List<KeelIssueRecord<?>> buffer);

    @Override
    public KeelIssueRecordRender<JsonObject> issueRecordRender() {
        return KeelIssueRecordRender.renderForJsonObject();
    }
}
