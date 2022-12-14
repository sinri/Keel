package io.github.sinri.keel.core.logger.async.adapter.aliyun.sls;

import io.github.sinri.keel.core.logger.LogMessage;
import io.github.sinri.keel.core.logger.async.AsyncLoggingServiceAdapter;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.ServiceLoader;

/**
 * USAGE IN PROJECT:
 * (1) prepare an implementation class of this interface, e.g. with com.aliyun.openservices::aliyun-log-producer::0.3.11;
 * (2) prepare file `src/main/resources/META-INF/services/io.github.sinri.keel.core.logger.async.adapter.aliyun.sls.AliyunSLSAdapter`;
 * (3) write one line of the class name prepared in (1) into the file described in (2).
 *
 * @since 2.9.3
 */
@Deprecated(since = "2.9.4")
public interface AliyunSLSAdapter extends AsyncLoggingServiceAdapter<LogMessage> {
    String IMPLEMENT = "async-aliyun-sls";
    Map<String, AliyunSLSAdapter> map = new HashMap<>();

    /**
     * @param topic Aliyun SLS Log Topic
     * @return get the cached (historic or fresh) adapter instance; if SPI load failed, null.
     */
    static AliyunSLSAdapter getInstanceForTopic(String topic) {
        if (topic == null) {
            return getInstanceForTopic("");
        }

        return map.computeIfAbsent(topic, s -> {
            ServiceLoader<AliyunSLSAdapter> serviceLoader = ServiceLoader.load(AliyunSLSAdapter.class);
            Optional<AliyunSLSAdapter> first = serviceLoader.findFirst();
            if (first.isEmpty()) {
                return null;
            }
            AliyunSLSAdapter aliyunSLSAdapter = first.get();
            aliyunSLSAdapter.setTopic(topic);
            return aliyunSLSAdapter;
        });
    }

    void setTopic(String topic);
}
