package io.github.sinri.keel.eventlogger.adapter;

import io.github.sinri.keel.eventlogger.KeelEventLog;
import io.github.sinri.keel.facade.Keel;
import io.vertx.core.Future;

import java.util.*;

/**
 * @since 2.9.4 实验性设计
 */
public interface AliyunSLSAdapter extends KeelEventLoggerAdapter {
    static AliyunSLSAdapter create() {
        ServiceLoader<AliyunSLSAdapter> serviceLoader = ServiceLoader.load(AliyunSLSAdapter.class);
        Optional<AliyunSLSAdapter> first = serviceLoader.findFirst();
        return first.orElse(null);
    }


    @Override
    default Future<Void> dealWithLogs(List<KeelEventLog> buffer) {
        Map<String, List<KeelEventLog>> topicMap = new HashMap<>();

        buffer.forEach(eventLog -> {
            String topic = eventLog.readString(KeelEventLog.RESERVED_KEY_TOPIC);
            if (topic == null) {
                topic = "";
            }
            topicMap.computeIfAbsent(topic, x -> new ArrayList<>())
                    .add(eventLog);
        });

        return Keel.getInstance().iterativelyCall(topicMap.keySet(), topic -> {
            return Future.succeededFuture()
                    .compose(v -> {
                        List<KeelEventLog> keelEventLogs = topicMap.get(topic);
                        return dealWithLogsForOneTopic(topic, keelEventLogs);
                    })
                    .compose(v -> {
                        return Future.succeededFuture();
                    }, throwable -> {
                        return Future.succeededFuture();
                    });
        });
    }

    Future<Void> dealWithLogsForOneTopic(String topic, List<KeelEventLog> buffer);
}
