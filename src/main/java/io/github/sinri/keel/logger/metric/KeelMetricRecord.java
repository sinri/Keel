package io.github.sinri.keel.logger.metric;

import io.github.sinri.keel.core.TechnicalPreview;
import io.github.sinri.keel.logger.issue.record.BaseIssueRecord;
import io.vertx.core.json.JsonObject;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

/**
 * @since 3.1.9 Technical Preview
 * @since 3.2.0 extends BaseIssueRecord
 * It is allowed to override this class, for fixed topic and metric.
 */
@TechnicalPreview(since = "3.1.9")
public class KeelMetricRecord extends BaseIssueRecord<KeelMetricRecord> {
    private final @NotNull String topic;
    private final @NotNull Map<String, String> labelMap = new HashMap<>();
    private final @NotNull String metricName;
    private final double value;

    public KeelMetricRecord(@NotNull String topic, @NotNull String metricName, double value) {
        super();
        this.topic = topic;
        this.metricName = metricName;
        this.value = value;
    }

    public JsonObject toJsonObject() {
        JsonObject labelObject = new JsonObject();
        labelMap.forEach(labelObject::put);
        return new JsonObject()
                .put("timestamp", timestamp())
                .put("labels", labelObject)
                .put("metric_name", metricName)
                .put("value", value);
    }


    @NotNull
    public String metricName() {
        return metricName;
    }

    public double value() {
        return value;
    }

    public Map<String, String> labels() {
        return labelMap;
    }

    public KeelMetricRecord label(String name, String value) {
        this.labelMap.put(name, value);
        return this;
    }


    @Override
    public @NotNull KeelMetricRecord getImplementation() {
        return this;
    }

    @Override
    public @NotNull String topic() {
        return topic;
    }
}