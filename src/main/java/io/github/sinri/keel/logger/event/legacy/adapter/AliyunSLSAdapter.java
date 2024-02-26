package io.github.sinri.keel.logger.event.legacy.adapter;

import io.github.sinri.keel.facade.async.KeelAsyncKit;
import io.github.sinri.keel.logger.event.legacy.KeelEventLog;
import io.vertx.core.Future;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

import static io.github.sinri.keel.helper.KeelHelpersInterface.KeelHelpers;

/**
 * @since 3.0.0
 */
@Deprecated(since = "3.2.0", forRemoval = true)
public interface AliyunSLSAdapter extends KeelEventLoggerAdapter {
    @Nonnull
    static AliyunSLSAdapter create() {
        ServiceLoader<AliyunSLSAdapter> serviceLoader = ServiceLoader.load(AliyunSLSAdapter.class);
        Optional<AliyunSLSAdapter> first = serviceLoader.findFirst();
        return first.orElseThrow();
    }

    @Override
    @Nonnull
    default Future<Void> dealWithLogs(@Nonnull List<KeelEventLog> buffer) {
        Map<String, List<KeelEventLog>> topicMap = new HashMap<>();

        buffer.forEach(eventLog -> {
            String topic = eventLog.topic();
            if (topic == null) {
                topic = "";
            }
            topicMap.computeIfAbsent(topic, x -> new ArrayList<>())
                    .add(eventLog);
        });

        return KeelAsyncKit.iterativelyCall(topicMap.keySet(), topic -> {
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

    @Nonnull
    Future<Void> dealWithLogsForOneTopic(@Nonnull String topic, @Nonnull List<KeelEventLog> buffer);

    @Override
    @Nullable
    default Object processThrowable(@Nullable Throwable throwable) {
        return KeelHelpers.jsonHelper().renderThrowableChain(throwable);
    }
}
