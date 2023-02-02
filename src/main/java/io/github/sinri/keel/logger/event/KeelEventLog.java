package io.github.sinri.keel.logger.event;

import io.github.sinri.keel.core.json.JsonifiableEntity;
import io.github.sinri.keel.helper.KeelHelpers;
import io.github.sinri.keel.logger.KeelLogLevel;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;

import java.util.Date;
import java.util.Objects;

public interface KeelEventLog extends JsonifiableEntity<KeelEventLog> {
    @Deprecated
    String RESERVED_KEY_CONTEXT = "context";
    String RESERVED_KEY_EVENT_MSG = "msg";
    String RESERVED_KEY_EVENT_EXCEPTION = "exception";
    //    String RESERVED_KEY_TIMESTAMP = "timestamp";
    String RESERVED_KEY_LEVEL = "level";
//    String RESERVED_KEY_THREAD_ID = "thread_id";
//    String RESERVED_KEY_CLUSTER_NODE_ID = "cluster_node_id";
//    String RESERVED_KEY_CLUSTER_NODE_ADDRESS = "cluster_node_address";

//    String RESERVED_KEY_TOPIC = "topic";

    static KeelEventLog create(KeelLogLevel level, String topic) {
        return new KeelEventLogImpl(level, topic);
    }

    static Future<String> render(KeelEventLog eventLog) {
        StringBuilder sb = new StringBuilder();

        String dateExpression = KeelHelpers.datetimeHelper().getDateExpression(new Date(eventLog.timestamp()), "yyyy-MM-dd HH:mm:ss.SSS");

        sb.append(dateExpression)
                .append(" [").append(eventLog.level()).append("]")
                .append(" <").append(eventLog.topic()).append(">")
                .append(" ").append(eventLog.message())
                .append("\n");
        JsonObject entries = new JsonObject();
        for (var k : eventLog.toJsonObject().fieldNames()) {
            //if (Objects.equals(k, RESERVED_KEY_TIMESTAMP)) continue;
            //if (Objects.equals(k, RESERVED_KEY_LEVEL)) continue;
            if (Objects.equals(k, RESERVED_KEY_EVENT_MSG)) continue;
            entries.put(k, eventLog.toJsonObject().getValue(k));
        }
        sb.append(KeelHelpers.jsonHelper().renderJsonToStringBlock("entries", entries));
        return Future.succeededFuture(sb.toString());
    }

    @Deprecated
    KeelEventLog context(String key, Object value);

    @Deprecated
    Object context(String key);

    KeelEventLog put(String key, Object value);

    Object get(String key);

    KeelEventLog timestamp(long timestamp);

    long timestamp();

    default String timestampExpression() {
        return timestampExpression("yyyy-MM-dd HH:mm:ss.SSS");
    }

    default String timestampExpression(String format) {
        return KeelHelpers.datetimeHelper().getDateExpression(new Date(timestamp()), format);
    }

    KeelEventLog level(KeelLogLevel level);

    KeelLogLevel level();

    KeelEventLog topic(String topic);

    String topic();

    KeelEventLog message(String msg);

    String message();
}
