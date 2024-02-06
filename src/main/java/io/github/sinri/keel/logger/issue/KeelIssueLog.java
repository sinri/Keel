package io.github.sinri.keel.logger.issue;

import io.github.sinri.keel.core.TechnicalPreview;
import io.github.sinri.keel.logger.KeelLogLevel;
import io.github.sinri.keel.logger.event.KeelEventLog;
import io.vertx.core.json.JsonObject;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @since 3.1.9 Technical Preview
 * Designed to cover the requirement that,
 * a certain kind of issues holds fixed attributes to record,
 * while the log index system accepts the pre-determined attribute names.
 */
@TechnicalPreview(since = "3.1.9")
public abstract class KeelIssueLog {
    private final List<String> classification = new ArrayList<>();
    private final JsonObject attributes = new JsonObject();
    private String topic;
    private KeelLogLevel level;

    protected void setTopic(String topic) {
        this.topic = topic;
    }

    protected void setLevel(KeelLogLevel level) {
        this.level = level;
    }

    protected void setClassification(@Nonnull List<String> classification) {
        this.classification.clear();
        this.classification.addAll(classification);
    }

    protected void setClassification(@Nonnull String... classification) {
        this.classification.clear();
        this.classification.addAll(Arrays.asList(classification));
    }

    protected void setAttribute(@Nonnull String name, @Nullable Object value) {
        if (value != null) {
            if (value instanceof BigDecimal) {
                this.attributes.put(name, ((BigDecimal) value).toPlainString());
            } else if (value instanceof Number) {
                this.attributes.put(name, value);
            } else {
                this.attributes.put(name, String.valueOf(value));
            }
        } else {
            this.attributes.put(name, null);
        }
    }


    @Nonnull
    public final String topic() {
        return this.topic;
    }

    @Nonnull
    public final KeelLogLevel level() {
        return this.level;
    }

    @Nonnull
    public final List<String> classification() {
        return classification;
    }

    @Nonnull
    public final JsonObject attributes() {
        return this.attributes;
    }

    public void toEventLog(@Nonnull KeelEventLog eventLog) {
        eventLog.level(level)
                .classification(classification)
                .topic(topic)
                .put("attributes", attributes);
    }
}
