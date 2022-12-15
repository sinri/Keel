package io.github.sinri.keel.eventlogger;

import io.github.sinri.keel.core.json.SimpleJsonifiableEntity;
import io.github.sinri.keel.facade.Keel;
import io.github.sinri.keel.lagecy.core.logger.KeelLogLevel;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

/**
 * @since 2.9.4 实验性设计
 */
public class KeelEventLog extends SimpleJsonifiableEntity {
    public static final String RESERVED_KEY_EVENT = "event";
    public static final String RESERVED_KEY_TIMESTAMP = "timestamp";
    public static final String RESERVED_KEY_LEVEL = "level";
    public static final String RESERVED_KEY_THREAD_ID = "thread_id";
    public static final String RESERVED_KEY_CLUSTER_NODE_ID = "cluster_node_id";
    public static final String RESERVED_KEY_CLUSTER_NODE_ADDRESS = "cluster_node_address";

    public static final String RESERVED_KEY_TOPIC = "topic";

    public KeelEventLog(KeelLogLevel level) {
        JsonObject jsonObject = new JsonObject()
                .put(RESERVED_KEY_TIMESTAMP, System.currentTimeMillis())
                .put(RESERVED_KEY_LEVEL, level.name())
                .put(RESERVED_KEY_THREAD_ID, Thread.currentThread().getId())
                .put(RESERVED_KEY_TOPIC, "")
                .put(RESERVED_KEY_EVENT, null);
        try {
            if (Keel.vertx().isClustered()) {
//                jsonObject.put("use_cluster", "YES");
                jsonObject
                        .put(RESERVED_KEY_CLUSTER_NODE_ID, Keel.getInstance().getVertxNodeID())
                        .put(RESERVED_KEY_CLUSTER_NODE_ADDRESS, Keel.getInstance().getVertxNodeNetAddress());
            } else {
//                jsonObject.put("use_cluster", "NO");
            }
//            jsonObject.put("use_vertx", "YES");
//            jsonObject.put("verticle_id", Keel.getVertx().getOrCreateContext().deploymentID());
        } catch (Throwable throwable) {
//            jsonObject.put("use_vertx", "NO");
        }

        reloadDataFromJsonObject(jsonObject);
    }

    /**
     * @param key   DO NOT USE RESERVED KEYS, i.e. io.github.sinri.keel.core.logger.event.KeelEventContext#RESERVED_KEY_EVENT
     * @param value Null, String, Number, JsonObject, JsonArray
     */
    public KeelEventLog put(String key, Object value) {
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

}
