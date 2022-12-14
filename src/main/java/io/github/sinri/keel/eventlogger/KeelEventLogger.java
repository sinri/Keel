package io.github.sinri.keel.eventlogger;

import io.github.sinri.keel.core.logger.KeelLogLevel;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;

/**
 * @since 2.9.4 实验性设计
 */
public interface KeelEventLogger {

//    KeelEventLogger setContextSupplier(Supplier<KeelEventContext> contextSupplier);

    void log(KeelEventLog eventLog);


    default void debug(String topic, JsonObject event) {
        log(new KeelEventLog(KeelLogLevel.DEBUG)
                .put(KeelEventLog.RESERVED_KEY_TOPIC, topic)
                .put(KeelEventLog.RESERVED_KEY_EVENT, event)
        );
    }


    default void info(String topic, JsonObject event) {
        log(new KeelEventLog(KeelLogLevel.INFO)
                .put(KeelEventLog.RESERVED_KEY_TOPIC, topic)
                .put(KeelEventLog.RESERVED_KEY_EVENT, event)
        );
    }


    default void notice(String topic, JsonObject event) {
        log(new KeelEventLog(KeelLogLevel.NOTICE)
                .put(KeelEventLog.RESERVED_KEY_TOPIC, topic)
                .put(KeelEventLog.RESERVED_KEY_EVENT, event)
        );
    }


    default void warning(String topic, JsonObject event) {
        log(new KeelEventLog(KeelLogLevel.WARNING)
                .put(KeelEventLog.RESERVED_KEY_TOPIC, topic)
                .put(KeelEventLog.RESERVED_KEY_EVENT, event)
        );
    }


    default void error(String topic, JsonObject event) {
        log(new KeelEventLog(KeelLogLevel.ERROR)
                .put(KeelEventLog.RESERVED_KEY_TOPIC, topic)
                .put(KeelEventLog.RESERVED_KEY_EVENT, event)
        );
    }


    default void fatal(String topic, JsonObject event) {
        log(new KeelEventLog(KeelLogLevel.FATAL)
                .put(KeelEventLog.RESERVED_KEY_TOPIC, topic)
                .put(KeelEventLog.RESERVED_KEY_LEVEL, KeelLogLevel.FATAL.name())
                .put(KeelEventLog.RESERVED_KEY_EVENT, event)
        );
    }

    Future<Void> gracefullyClose();

    default KeelEventLoggerWrapper generateWrapperForTopic(String presetTopic) {
        return new KeelEventLoggerWrapperImpl(presetTopic, () -> this);
    }
}
