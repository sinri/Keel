package io.github.sinri.keel.logger.event.legacy;

import io.github.sinri.keel.core.json.JsonifiableEntity;
import io.github.sinri.keel.helper.KeelDateTimeHelper;
import io.github.sinri.keel.logger.KeelLogLevel;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import static io.github.sinri.keel.helper.KeelHelpersInterface.KeelHelpers;

@Deprecated(since = "3.2.0")
public interface KeelEventLog extends JsonifiableEntity<KeelEventLog> {
    String RESERVED_KEY_EVENT_MSG = "msg";
    String RESERVED_KEY_EVENT_EXCEPTION = "exception";
    String RESERVED_KEY_CONTEXT = "context";
    String RESERVED_KEY_CLASSIFICATION = "classification";

    static KeelEventLog create(@Nonnull KeelLogLevel level, @Nonnull String topic) {
        return new KeelEventLogImpl(level, topic);
    }

    static Future<String> render(@Nonnull KeelEventLog eventLog) {
        StringBuilder sb = new StringBuilder();

        String dateExpression = KeelHelpers.datetimeHelper().getDateExpression(new Date(eventLog.timestamp()), "yyyy-MM-dd HH:mm:ss.SSS");

        sb.append("㏒").append("\t")
                .append(dateExpression)
                .append(" [").append(eventLog.level()).append("]")
                .append(" <").append(eventLog.topic()).append(">")
                .append(" ").append(eventLog.message())
        ;
        JsonObject entries = new JsonObject();
        for (var k : eventLog.toJsonObject().fieldNames()) {
            if (Objects.equals(k, RESERVED_KEY_EVENT_MSG)) continue;
            entries.put(k, eventLog.toJsonObject().getValue(k));
        }
        if (!entries.isEmpty()) {
            sb.append("\n").append(KeelHelpers.jsonHelper().renderJsonToStringBlock("entries", entries));
        }
        return Future.succeededFuture(sb.toString());
    }

//    @Deprecated(since = "3.1.10", forRemoval = true)
//    KeelEventLog put(@Nonnull String key, @Nullable Object value);
//
//    @Deprecated(since = "3.1.10", forRemoval = true)
//    @Nullable
//    Object get(@Nonnull String key);

    /**
     * @since 3.1.10
     */
    default KeelEventLog context(@Nullable Handler<JsonObject> contextHandler) {
        JsonObject context = new JsonObject();
        contextHandler.handle(context);
        this.context(context);
        return this;
    }

    /**
     * @since 3.1.10
     */
    KeelEventLog context(@Nullable JsonObject context);

    /**
     * @since 3.1.10
     */
    @Nullable
    JsonObject context();

    KeelEventLog timestamp(long timestamp);

    long timestamp();

    @Nonnull
    default String timestampExpression() {
        return timestampExpression(KeelDateTimeHelper.MYSQL_DATETIME_MS_PATTERN);
    }

    @Nonnull
    default String timestampExpression(@Nonnull String format) {
        return KeelHelpers.datetimeHelper().getDateExpression(new Date(timestamp()), format);
    }

    KeelEventLog level(@Nonnull KeelLogLevel level);

    @Nonnull
    KeelLogLevel level();

    KeelEventLog topic(String topic);

    @Nonnull
    String topic();

    KeelEventLog message(@Nullable String msg);

    @Nullable
    String message();

    /**
     * @since 3.1.7
     * Under one topic, designed a detail-able event classification method, to determine each event stands for.
     */
    KeelEventLog classification(@Nonnull List<String> classification);

    /**
     * @since 3.1.7
     * Under one topic, designed a detail-able event classification method, to determine each event stands for.
     */
    KeelEventLog classification(@Nonnull String... classificationItems);

    /**
     * @since 3.1.10
     */
    KeelEventLog exception(Object processedThrowable);

    /**
     * @since 3.1.10
     */
    Object exception();
}
