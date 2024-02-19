package io.github.sinri.keel.logger.metric;

import io.github.sinri.keel.core.TechnicalPreview;
import io.github.sinri.keel.core.json.SimpleJsonifiableEntity;
import io.vertx.core.json.JsonObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @since 3.1.9 Technical Preview
 */
@TechnicalPreview(since = "3.1.9")
public class KeelMetricRecord extends SimpleJsonifiableEntity {
    public KeelMetricRecord(String metricName, double value) {
        this.jsonObject = new JsonObject()
                .put("timestamp", System.currentTimeMillis())
                .put("labels", new JsonObject());
        this.metricName(metricName);
        this.value(value);
    }

    public long timestamp() {
        return Objects.requireNonNull(readLong("timestamp"));
    }

    public KeelMetricRecord timestamp(long timestamp) {
        this.jsonObject.put("timestamp", timestamp);
        return this;
    }

    public String topic() {
        return Objects.requireNonNull(readString("topic"));
    }

    public KeelMetricRecord topic(String topic) {
        this.jsonObject.put("topic", topic);
        return this;
    }

    public String metricName() {
        return Objects.requireNonNull(readString("metric_name"));
    }

    public KeelMetricRecord metricName(String metricName) {
        this.jsonObject.put("metric_name", metricName);
        return this;
    }

    public double value() {
        return Objects.requireNonNull(readDouble("value"));
    }

    public KeelMetricRecord value(double value) {
        this.jsonObject.put("value", value);
        return this;
    }

    public Map<String, String> labels() {
        var x = readJsonObject("labels");
        if (x == null) {
            return Map.of();
        }
        Map<String, String> map = new HashMap<>();
        x.forEach(entry -> {
            map.put(entry.getKey(), String.valueOf(entry.getValue()));
        });
        return map;
    }

    public KeelMetricRecord label(String name, String value) {
        this.jsonObject.getJsonObject("labels")
                .put(name, value);
        return this;
    }
}