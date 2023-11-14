package io.github.sinri.keel.logger.event;

import io.github.sinri.keel.logger.KeelLogLevel;
import io.vertx.core.json.JsonObject;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class KeelEventLogImpl implements KeelEventLog {
    private @Nonnull JsonObject jsonObject;
    private long timestamp;
    private @Nonnull String topic;
    private @Nonnull KeelLogLevel level;

    public KeelEventLogImpl(@Nonnull KeelLogLevel level, @Nonnull String topic) {
        this.jsonObject = new JsonObject();
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


    @Override
    public KeelEventLog put(@Nonnull String key, @Nullable Object value) {
        this.jsonObject.put(key, value);
        return this;
    }

    @Override
    @Nullable
    public Object get(@Nonnull String key) {
        return this.jsonObject.getValue(key);
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
        //return put(RESERVED_KEY_LEVEL, level.name());
    }

    @Override
    @Nonnull
    public KeelLogLevel level() {
        return level;
        //return KeelLogLevel.valueOf(readString(RESERVED_KEY_LEVEL));
    }

    @Override
    public KeelEventLog topic(String topic) {
        this.topic = topic;
        return this;
    }

    @Override
    @Nonnull
    public String topic() {
        return this.topic;
    }

    @Override
    public KeelEventLog message(String msg) {
        return put(RESERVED_KEY_EVENT_MSG, msg);
    }

    @Override
    @Nullable
    public String message() {
        return readString(RESERVED_KEY_EVENT_MSG);
    }

    @Override
    public String toString() {
        return timestampExpression() + " [" + level() + "] " + toJsonObject();
    }
}
