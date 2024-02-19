package io.github.sinri.keel.logger.metric;

import io.github.sinri.keel.core.TechnicalPreview;
import io.github.sinri.keel.facade.async.KeelAsyncKit;
import io.vertx.core.Future;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @since 3.1.9 Technical Preview
 */
@TechnicalPreview(since = "3.1.9")
abstract public class KeelMetricRecorder {
    private final AtomicBoolean endSwitch = new AtomicBoolean(false);
    private final Queue<KeelMetricRecord> metricRecordQueue = new ConcurrentLinkedQueue<>();

    public void recordMetric(KeelMetricRecord metricRecord) {
        this.metricRecordQueue.add(metricRecord);
    }

    protected int bufferSize() {
        return 1000;
    }

    public void start() {
        KeelAsyncKit.repeatedlyCall(routineResult -> {
            return Future.succeededFuture()
                    .compose(v -> {
                        List<KeelMetricRecord> buffer = new ArrayList<>();

                        while (true) {
                            KeelMetricRecord metricRecord = metricRecordQueue.poll();
                            if (metricRecord == null) break;

                            buffer.add(metricRecord);
                            if (buffer.size() >= bufferSize()) break;
                        }

                        if (buffer.isEmpty()) {
                            if (endSwitch.get()) {
                                routineResult.stop();
                                return Future.succeededFuture();
                            }
                            return KeelAsyncKit.sleep(1000L);
                        } else {
                            Map<String, List<KeelMetricRecord>> map = groupByTopic(buffer);
                            return KeelAsyncKit.iterativelyCall(map.keySet(), topic -> {
                                return handleForTopic(topic, map.get(topic));
                            });
                        }
                    });
        });
    }

    public void end() {
        endSwitch.set(true);
    }

    private Map<String, List<KeelMetricRecord>> groupByTopic(List<KeelMetricRecord> buffer) {
        Map<String, List<KeelMetricRecord>> byTopicMap = new HashMap<>();

        buffer.forEach(metricRecord -> byTopicMap.computeIfAbsent(metricRecord.topic(), s -> new ArrayList<>())
                .add(metricRecord));

        return byTopicMap;
    }

    abstract protected Future<Void> handleForTopic(String topic, List<KeelMetricRecord> buffer);
}
