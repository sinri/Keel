package io.github.sinri.keel.core.logger;

import io.github.sinri.keel.Keel;
import io.vertx.core.json.JsonObject;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class LogMessage {
    private final long timestamp;
    private final Map<String, String> map;

    public LogMessage(
            long timestamp,
            KeelLogLevel logLevel,
            String aspect,
            String message,
            JsonObject context
    ) {
        this.timestamp = timestamp;
        this.map = new HashMap<>();
        this.map.put("log_aspect", aspect);
        this.map.put("log_level", logLevel.name());
        this.map.put("log_time", Keel.helpers().datetime().getDateExpression(new Date(timestamp), "yyyy-MM-dd HH:mm:ss.SSS"));
        this.map.put("log_content", message);
        if (context != null) {
            this.map.put("log_context", context.toString());
        }
    }

    public LogMessage enrich(String key, String value) {
        this.map.put(key, value);
        return this;
    }

    public LogMessage setLogThread(long threadID) {
        return enrich("log_thread", String.valueOf(threadID));
    }

    public LogMessage setLogVerticle(String verticle) {
        return enrich("log_verticle", String.valueOf(verticle));
    }

    public LogMessage setLogContext(JsonObject context) {
        return enrich("log_context", context == null ? null : context.toString());
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
        sb.append(map.get("log_time"))
                .append(" [").append(map.get("log_level")).append("]")
                .append(" <").append(map.get("log_aspect")).append(">");

        map.forEach((k, v) -> {
            if (Objects.equals("log_time", k)) {
                return;
            }
            if (Objects.equals("log_aspect", k)) {
                return;
            }
            if (Objects.equals("log_level", k)) {
                return;
            }
            if (Objects.equals("log_content", k)) {
                return;
            }
            if (Objects.equals("log_context", k)) {
                return;
            }

            sb.append(" ").append(k).append("=").append(v);
        });

        sb.append(" ").append(map.get("log_content"));
        String log_context = map.get("log_context");
        if (log_context != null) {
            sb.append(" | ").append(log_context);
        }
        return sb.toString();
    }
}
