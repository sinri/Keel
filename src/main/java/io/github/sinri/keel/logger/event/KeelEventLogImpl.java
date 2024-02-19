package io.github.sinri.keel.logger.event;

import io.github.sinri.keel.logger.KeelLogLevel;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class KeelEventLogImpl implements KeelEventLog {
    private @Nonnull JsonObject jsonObject;
    private long timestamp;
    private @Nonnull String topic;
    private @Nonnull KeelLogLevel level;

    public KeelEventLogImpl(@Nonnull KeelLogLevel level, @Nonnull String topic) {
        this.jsonObject = new JsonObject()
                .put(RESERVED_KEY_CONTEXT, new JsonObject());
        this.timestamp = System.currentTimeMillis();
        this.level = level;
        this.topic = topic;
    }

    @Nonnull
    @Override
    public JsonObject toJsonObject() {
        return jsonObject;
    }

    @Nonnull
    @Override
    public KeelEventLog reloadDataFromJsonObject(@Nonnull JsonObject jsonObject) {
        this.jsonObject = jsonObject;
        return this;
    }

    /**
     * @since 3.1.10 protected
     */
    protected KeelEventLog put(@Nonnull String key, @Nullable Object value) {
        this.jsonObject.put(key, value);
        return this;
    }

    /**
     * @since 3.1.10 protected
     */
    @Nullable
    protected Object get(@Nonnull String key) {
        return this.jsonObject.getValue(key);
    }

    /**
     * @param context a json object
     * @since 3.1.10
     */
    @Override
    public KeelEventLog context(@Nullable JsonObject context) {
        this.put(RESERVED_KEY_CONTEXT, context);
        return this;
    }

    /**
     * @since 3.1.10
     */
    @Nullable
    @Override
    public JsonObject context() {
        return this.jsonObject.getJsonObject(RESERVED_KEY_CONTEXT);
    }


    @Override
    public KeelEventLog timestamp(long timestamp) {
        this.timestamp = timestamp;
        return this;
    }

    @Override
    public long timestamp() {
        return this.timestamp;
    }

    @Override
    @Nullable
    public KeelEventLog level(@Nonnull KeelLogLevel level) {
        this.level = level;
        return this;
    }

    @Override
    @Nonnull
    public KeelLogLevel level() {
        return level;
    }

    @Override
    public KeelEventLog topic(@Nonnull String topic) {
        this.topic = topic;
        return this;
    }

    @Override
    @Nonnull
    public String topic() {
        return this.topic;
    }

    @Override
    public KeelEventLog message(@Nullable String msg) {
        return put(RESERVED_KEY_EVENT_MSG, msg);
    }

    @Override
    @Nullable
    public String message() {
        return readString(RESERVED_KEY_EVENT_MSG);
    }

    /**
     * @since 3.1.10 Moved into impl.
     */
    @Override
    public KeelEventLog classification(@Nonnull List<String> classification) {
        if (!classification.isEmpty()) {
            this.put(RESERVED_KEY_CLASSIFICATION, new JsonArray(classification));
        }
        return this;
    }

    /**
     * @since 3.1.10 Moved into impl.
     */
    @Override
    public KeelEventLog classification(@Nonnull String... classificationItems) {
        var classification = new JsonArray();
        for (var c : classificationItems) {
            classification.add(c);
        }
        this.put(RESERVED_KEY_CLASSIFICATION, classification);
        return this;
    }

    /**
     * @since 3.1.10
     */
    @Override
    public KeelEventLog exception(Object processedThrowable) {
        return this.put(RESERVED_KEY_EVENT_EXCEPTION, processedThrowable);
    }

    /**
     * @since 3.1.10
     */
    @Override
    public Object exception() {
        return this.get(RESERVED_KEY_EVENT_EXCEPTION);
    }

    @Override
    public String toString() {
        return timestampExpression() + " [" + level() + "] " + toJsonObject();
    }
}
