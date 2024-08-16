package io.github.sinri.keel.logger.issue.record;

import io.github.sinri.keel.core.TechnicalPreview;
import io.github.sinri.keel.core.json.UnmodifiableJsonifiableEntity;
import io.github.sinri.keel.logger.KeelLogLevel;
import io.vertx.core.json.JsonObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * @since 3.1.10
 */
@TechnicalPreview(since = "3.1.10")
public abstract class BaseIssueRecord<T> implements KeelIssueRecord<T> {
    private final @NotNull JsonObject attributes;
    private final @NotNull List<String> classification;
    private long timestamp;
    private @NotNull KeelLogLevel level;
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
    final public T level(@NotNull KeelLogLevel level) {
        this.level = level;
        return getImplementation();
    }


    @Override
    final public @NotNull KeelLogLevel level() {
        return level;
    }

    @Override
    final public T classification(@NotNull List<String> classification) {
//        this.attribute(IssueRecordClassificationMixin.AttributeClassification, new JsonArray(classification));
        this.classification.clear();
        this.classification.addAll(classification);
        return getImplementation();
    }

    @Override
    final public @NotNull List<String> classification() {
//        var array = this.attributes().getJsonArray(IssueRecordClassificationMixin.AttributeClassification);
//        List<String> list = new ArrayList<>();
//        if (array != null) {
//            array.forEach(x -> list.add(String.valueOf(x)));
//        }
//        return list;
        return classification;
    }

    final protected void attribute(@NotNull String name, @Nullable Object value) {
        if (
                AttributeLevel.equalsIgnoreCase(name)
                        || AttributeException.equalsIgnoreCase(name)
                        || AttributeClassification.equalsIgnoreCase(name)
        ) throw new IllegalArgumentException("Attribute name `" + name + "` reserved");
        attributes.put(name, value);
    }

    @Override
    final public @NotNull UnmodifiableJsonifiableEntity attributes() {
        return UnmodifiableJsonifiableEntity.wrap(attributes);
    }

    @Override
    final public T exception(@NotNull Throwable throwable) {
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
    final public T message(@NotNull String message) {
        this.attribute(IssueRecordMessageMixin.AttributeMessage, message);
        return getImplementation();
    }

    @Override
    public T context(@NotNull JsonObject context) {
        this.attribute(AttributeContext, context);
        return this.getImplementation();
    }
}
