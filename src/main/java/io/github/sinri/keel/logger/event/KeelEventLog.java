package io.github.sinri.keel.logger.event;

import io.github.sinri.keel.core.json.SimpleJsonifiableEntity;
import io.github.sinri.keel.facade.Keel3;
import io.github.sinri.keel.helper.KeelHelpers;
import io.github.sinri.keel.logger.KeelLogLevel;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.Date;
import java.util.Objects;

/**
 * @since 2.9.4 实验性设计
 */
public class KeelEventLog extends SimpleJsonifiableEntity {
    public static final String RESERVED_KEY_EVENT = "event";
    public static final String RESERVED_KEY_EVENT_MSG = "msg";
    public static final String RESERVED_KEY_EVENT_EXCEPTION = "exception";
    public static final String RESERVED_KEY_TIMESTAMP = "timestamp";
    public static final String RESERVED_KEY_LEVEL = "level";
    public static final String RESERVED_KEY_THREAD_ID = "thread_id";
    public static final String RESERVED_KEY_CLUSTER_NODE_ID = "cluster_node_id";
    public static final String RESERVED_KEY_CLUSTER_NODE_ADDRESS = "cluster_node_address";

    public static final String RESERVED_KEY_TOPIC = "topic";

    public KeelEventLog() {
        this(KeelLogLevel.INFO);
    }

    public KeelEventLog(KeelLogLevel level) {
        super();
        timestamp(System.currentTimeMillis());
        topic("");
        level(level);
        this.toJsonObject().put(RESERVED_KEY_EVENT, new JsonObject());
    }

    public KeelEventLog(JsonObject context) {
        super(context);
        if (context.getString(RESERVED_KEY_TIMESTAMP) == null) {
            timestamp(System.currentTimeMillis());
        }
        if (context.getString(RESERVED_KEY_LEVEL) == null) {
            level(KeelLogLevel.INFO);
        }
        if (this.toJsonObject().getJsonObject(RESERVED_KEY_EVENT) == null) {
            this.toJsonObject().put(RESERVED_KEY_EVENT, new JsonObject());
        }
    }

    /**
     * @param key   DO NOT USE RESERVED KEYS, i.e. io.github.sinri.keel.core.logger.event.KeelEventContext#RESERVED_KEY_EVENT
     * @param value Null, String, Number, JsonObject, JsonArray
     */
    public KeelEventLog context(String key, Object value) {
        if (Objects.equals(key, RESERVED_KEY_EVENT)) {
            System.err.println(getClass().getName() + "::putContext WARNING: EVENT DATA SET TO CONTEXT (" + value + ")");
            return this;
        }
        if (
                value == null
                        || value instanceof String
                        || value instanceof Number
                        || value instanceof JsonObject
                        || value instanceof JsonArray
        ) {
            this.toJsonObject().put(key, value);
        } else {
            this.toJsonObject().put(key, String.valueOf(value));
        }

        return this;
    }

    public KeelEventLog put(String key, Object value) {
        if (
                value == null
                        || value instanceof String
                        || value instanceof Number
                        || value instanceof JsonObject
                        || value instanceof JsonArray
        ) {
            this.toJsonObject().getJsonObject(RESERVED_KEY_EVENT).put(key, value);
        } else {
            this.toJsonObject().getJsonObject(RESERVED_KEY_EVENT).put(key, String.valueOf(value));
        }

        return this;
    }

    public KeelEventLog timestamp(long timestamp) {
        this.context(RESERVED_KEY_TIMESTAMP, KeelHelpers.datetimeHelper().getDateExpression(new Date(timestamp), "yyyy-MM-dd HH:mm:ss.SSS"));
        return this;
    }

    public KeelEventLog level(KeelLogLevel level) {
        this.context(RESERVED_KEY_LEVEL, level.name());
        return this;
    }

    public KeelEventLog topic(String topic) {
        this.toJsonObject().put(RESERVED_KEY_TOPIC, topic);
        return this;
    }

    public KeelEventLog message(String msg) {
        this.put(RESERVED_KEY_EVENT_MSG, msg);
        return this;
    }

    public KeelEventLog injectContext() {
        this.context(KeelEventLog.RESERVED_KEY_THREAD_ID, Thread.currentThread().getId());
        if (Keel3.isRunningInVertxCluster()) {
//                jsonObject.put("use_cluster", "YES");
            this.context(KeelEventLog.RESERVED_KEY_CLUSTER_NODE_ID, Keel3.getVertxNodeID());
            this.context(KeelEventLog.RESERVED_KEY_CLUSTER_NODE_ADDRESS, Keel3.getVertxNodeNetAddress());
        } else {
//                jsonObject.put("use_cluster", "NO");
        }
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(this.jsonObject.getString(RESERVED_KEY_TIMESTAMP))
                .append(" [").append(this.jsonObject.getString(RESERVED_KEY_LEVEL)).append("]");
        JsonObject context = new JsonObject();
        for (var k : this.jsonObject.fieldNames()) {
            if (Objects.equals(k, RESERVED_KEY_TIMESTAMP)) break;
            if (Objects.equals(k, RESERVED_KEY_LEVEL)) break;
            if (Objects.equals(k, RESERVED_KEY_EVENT)) break;
            context.put(k, this.jsonObject.getValue(k));
        }
        sb.append(" ").append(context);

        JsonObject event = this.jsonObject.getJsonObject(RESERVED_KEY_EVENT);
        for (var k : event.fieldNames()) {
            sb.append("\n").append(k).append(": ").append(event.getValue(k));
        }
        return sb.toString();
    }
}
