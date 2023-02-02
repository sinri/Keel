package io.github.sinri.keel.logger.event;

import io.github.sinri.keel.logger.KeelLogLevel;
import io.vertx.core.json.JsonObject;
import org.jetbrains.annotations.NotNull;

public class KeelEventLogImpl implements KeelEventLog {
    private JsonObject jsonObject;
    private long timestamp;
    private String topic;
    private KeelLogLevel level;

    public KeelEventLogImpl(KeelLogLevel level, String topic) {
        this.jsonObject = new JsonObject();
        this.timestamp(System.currentTimeMillis());
        this.level(level);
        this.topic(topic);
    }

    @NotNull
    @Override
    public JsonObject toJsonObject() {
        return jsonObject;
    }

    @NotNull
    @Override
    public KeelEventLog reloadDataFromJsonObject(JsonObject jsonObject) {
        this.jsonObject = jsonObject;
        return this;
    }

    @Override
    @Deprecated
    public KeelEventLog context(String key, Object value) {
        JsonObject context = this.jsonObject.getJsonObject(KeelEventLog.RESERVED_KEY_CONTEXT);
        if (context == null) {
            context = new JsonObject();
            this.jsonObject.put(KeelEventLog.RESERVED_KEY_CONTEXT, context);
        }
        context.put(key, value);
        return this;
    }

    @Override
    @Deprecated
    public Object context(String key) {
        return this.readValue(KeelEventLog.RESERVED_KEY_CONTEXT, key);
    }

    @Override
    public KeelEventLog put(String key, Object value) {
        this.jsonObject.put(key, value);
        return this;
    }

    @Override
    public Object get(String key) {
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
    public KeelEventLog level(KeelLogLevel level) {
        this.level = level;
        return this;
        //return put(RESERVED_KEY_LEVEL, level.name());
    }

    @Override
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
    public String topic() {
        return this.topic;
    }

    @Override
    public KeelEventLog message(String msg) {
        return put(RESERVED_KEY_EVENT_MSG, msg);
    }

    @Override
    public String message() {
        return readString(RESERVED_KEY_EVENT_MSG);
    }

    @Override
    public String toString() {
        return timestampExpression() + " [" + level() + "] " + toJsonObject();
    }
}
