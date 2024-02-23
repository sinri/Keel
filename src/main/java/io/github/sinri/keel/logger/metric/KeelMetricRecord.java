package io.github.sinri.keel.logger.metric;

import io.github.sinri.keel.core.TechnicalPreview;
import io.github.sinri.keel.logger.issue.record.BaseIssueRecord;
import io.vertx.core.json.JsonObject;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;

/**
 * @since 3.1.9 Technical Preview
 * @since 3.2.0 extends BaseIssueRecord
 * It is allowed to override this class, for fixed topic and metric.
 */
@TechnicalPreview(since = "3.1.9")
public class KeelMetricRecord extends BaseIssueRecord<KeelMetricRecord> {
    private final @Nonnull String topic;
    private final @Nonnull Map<String, String> labelMap = new HashMap<>();
    private final @Nonnull String metricName;
    private final double value;

    public KeelMetricRecord(@Nonnull String topic, @Nonnull String metricName, double value) {
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


    @Nonnull
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

    @Nonnull
    @Override
    public KeelMetricRecord getImplementation() {
        return this;
    }

    @Nonnull
    @Override
    public String topic() {
        return topic;
    }
}