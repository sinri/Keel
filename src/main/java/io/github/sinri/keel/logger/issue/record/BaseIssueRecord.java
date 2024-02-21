package io.github.sinri.keel.logger.issue.record;

import io.github.sinri.keel.core.TechnicalPreview;
import io.github.sinri.keel.core.json.UnmodifiableJsonifiableEntity;
import io.github.sinri.keel.logger.KeelLogLevel;
import io.vertx.core.json.JsonObject;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * @since 3.1.10
 */
@TechnicalPreview(since = "3.1.10")
public abstract class BaseIssueRecord<T> implements KeelIssueRecord<T>, IssueRecordMessageMixin<T> {
    private final @Nonnull JsonObject attributes;
    private final @Nonnull List<String> classification;
    private long timestamp;
    private @Nonnull KeelLogLevel level;
    private @Nullable Throwable exception;

    public BaseIssueRecord() {
        this.timestamp = System.currentTimeMillis();
        this.attributes = new JsonObject();
        this.level = KeelLogLevel.INFO;
        this.classification = new ArrayList<>();
    }

    @Override
    final public T timestamp(long timestamp) {
        this.timestamp = timestamp;
        return getImplementation();
    }

    @Override
    final public long timestamp() {
        return timestamp;
    }

    @Override
    final public T level(@Nonnull KeelLogLevel level) {
        this.level = level;
        return getImplementation();
    }

    @Nonnull
    @Override
    final public KeelLogLevel level() {
        return level;
    }

    @Override
    final public T classification(@Nonnull List<String> classification) {
//        this.attribute(IssueRecordClassificationMixin.AttributeClassification, new JsonArray(classification));
        this.classification.clear();
        this.classification.addAll(classification);
        return getImplementation();
    }

    @Nonnull
    @Override
    final public List<String> classification() {
//        var array = this.attributes().getJsonArray(IssueRecordClassificationMixin.AttributeClassification);
//        List<String> list = new ArrayList<>();
//        if (array != null) {
//            array.forEach(x -> list.add(String.valueOf(x)));
//        }
//        return list;
        return classification;
    }

    final protected void attribute(@Nonnull String name, @Nullable Object value) {
        if (
                AttributeLevel.equalsIgnoreCase(name)
                        || AttributeException.equalsIgnoreCase(name)
                        || AttributeClassification.equalsIgnoreCase(name)
        ) throw new IllegalArgumentException("Attribute name `" + name + "` reserved");
        attributes.put(name, value);
    }

    @Nonnull
    @Override
    final public UnmodifiableJsonifiableEntity attributes() {
        return UnmodifiableJsonifiableEntity.wrap(attributes);
    }

    @Override
    final public T exception(@Nonnull Throwable throwable) {
        this.exception = throwable;
        return getImplementation();
    }

    @Nullable
    @Override
    final public Throwable exception() {
        return exception;
    }

    @Nullable
    @Override
    final public String message() {
        return this.attributes.getString(IssueRecordMessageMixin.AttributeMessage);
    }

    @Override
    final public T message(@Nonnull String message) {
        this.attribute(IssueRecordMessageMixin.AttributeMessage, message);
        return getImplementation();
    }
}
