package io.github.sinri.keel.core.logger;

import io.github.sinri.keel.Keel;
import io.vertx.core.json.JsonObject;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @since 2.9
 * @since 2.9.1 fields const
 * @since 2.9.2 add category
 * @since 2.9.3 add log_cluster_node_id and log_cluster_node_address
 */
public class LogMessage {
    public static String KeyLogAspect = "log_aspect";
    public static String KeyLogLevel = "log_level";
    public static String KeyLogTime = "log_time";
    public static String KeyLogContent = "log_content";
    public static String KeyLogContext = "log_context";
    public static String KeyLogThread = "log_thread";
    public static String KeyLogVerticle = "log_verticle";
    public static String KeyLogCategory = "log_category";
    public static String KeyLogClusterNodeId = "log_cluster_node_id";
    public static String KeyLogClusterNodeAddress = "log_cluster_node_address";

    private final long timestamp;
    private final Map<String, String> map;

    public LogMessage(
            long timestamp,
            KeelLogLevel logLevel,
            String aspect,
            String message,
            String category,
            JsonObject context
    ) {
        this.timestamp = timestamp;
        this.map = new HashMap<>();
        this.map.put(KeyLogAspect, aspect);
        this.map.put(KeyLogLevel, logLevel.name());
        this.map.put(KeyLogTime, Keel.helpers().datetime().getDateExpression(new Date(timestamp), "yyyy-MM-dd HH:mm:ss.SSS"));
        this.map.put(KeyLogContent, message);
        this.map.put(KeyLogCategory, Objects.requireNonNullElse(category, ""));
        if (context != null) {
            this.map.put(KeyLogContext, context.toString());
//            context.forEach(entry -> {
//                this.map.put(entry.getKey(), String.valueOf(entry.getValue()));
//            });
        }

        // cluster node
        String vertxNodeNetAddress = Keel.getVertxNodeNetAddress();
        String vertxNodeID = Keel.getVertxNodeID();
        if (Keel.getVertxNodeNetAddress() != null && vertxNodeID != null) {
            this.map.put(KeyLogClusterNodeId, vertxNodeID);
            this.map.put(KeyLogClusterNodeAddress, vertxNodeNetAddress);
        }
    }

    public LogMessage enrich(String key, String value) {
        this.map.put(key, value);
        return this;
    }

    public LogMessage setLogThread(long threadID) {
        return enrich(KeyLogThread, String.valueOf(threadID));
    }

    public LogMessage setLogVerticle(String verticle) {
        return enrich(KeyLogVerticle, String.valueOf(verticle));
    }

    /**
     * @since 2.9.1
     */
    public JsonObject getLogContext() {
        String s = map.get(KeyLogContext);
        if (s == null) return null;
        if (Objects.equals("null", s) || s.isEmpty() || s.isBlank()) {
            return null;
        }
        try {
            return new JsonObject(s);
        } catch (Throwable e) {
            return null;
        }
    }

    public LogMessage setLogContext(JsonObject context) {
        return enrich(KeyLogContext, context == null ? null : context.toString());
    }

    public long getTimestamp() {
        return timestamp;
    }

    public Map<String, String> getMap() {
        return map;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(map.get(KeyLogTime))
                .append(" [").append(map.get(KeyLogLevel)).append("]")
                .append(" <").append(map.get(KeyLogAspect)).append(">");

        map.forEach((k, v) -> {
            if (Objects.equals(KeyLogTime, k)) {
                return;
            }
            if (Objects.equals(KeyLogAspect, k)) {
                return;
            }
            if (Objects.equals(KeyLogLevel, k)) {
                return;
            }
            if (Objects.equals(KeyLogContent, k)) {
                return;
            }
            if (Objects.equals(KeyLogContext, k)) {
                return;
            }

            sb.append(" ").append(k).append("=").append(v);
        });

        sb.append(" ").append(map.get(KeyLogContent));
        String log_context = map.get(KeyLogContext);
        if (log_context != null) {
            sb.append(" | ").append(log_context);
        }
        return sb.toString();
    }
}
