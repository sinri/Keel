package io.github.sinri.keel.logger.event;

import io.github.sinri.keel.logger.KeelLogLevel;
import io.github.sinri.keel.logger.event.logger.KeelOutputEventLogger;
import io.github.sinri.keel.logger.event.logger.KeelSilentEventLogger;
import io.vertx.core.Handler;

import javax.annotation.Nonnull;
import java.util.function.Supplier;

/**
 * @since 2.9.4
 */
public interface KeelEventLogger {
    static KeelEventLogger silentLogger() {
        return KeelSilentEventLogger.getInstance();
    }

    @Deprecated
    static KeelEventLogger outputLogger() {
        return KeelOutputEventLogger.getInstance();
    }

    Supplier<KeelEventLogCenter> getEventLogCenterSupplier();

    String getPresetTopic();


    default void log(@Nonnull Handler<KeelEventLog> eventLogHandler) {
        KeelEventLog eventLog = new KeelEventLog();
        eventLog.injectContext();
        eventLogHandler.handle(eventLog);
        getEventLogCenterSupplier().get().log(eventLog);
    }

    default void debug(@Nonnull Handler<KeelEventLog> eventLogHandler) {
        log(eventLog -> {
            eventLog.level(KeelLogLevel.DEBUG);
            eventLog.topic(getPresetTopic());
            eventLogHandler.handle(eventLog);
        });
    }

    default void info(@Nonnull Handler<KeelEventLog> eventLogHandler) {
        log(eventLog -> {
            eventLog.level(KeelLogLevel.INFO);
            eventLog.topic(getPresetTopic());
            eventLogHandler.handle(eventLog);
        });
    }

    default void notice(@Nonnull Handler<KeelEventLog> eventLogHandler) {
        log(eventLog -> {
            eventLog.level(KeelLogLevel.NOTICE);
            eventLog.topic(getPresetTopic());
            eventLogHandler.handle(eventLog);
        });
    }

    default void warning(@Nonnull Handler<KeelEventLog> eventLogHandler) {
        log(eventLog -> {
            eventLog.level(KeelLogLevel.WARNING);
            eventLog.topic(getPresetTopic());
            eventLogHandler.handle(eventLog);
        });
    }

    default void error(@Nonnull Handler<KeelEventLog> eventLogHandler) {
        log(eventLog -> {
            eventLog.level(KeelLogLevel.ERROR);
            eventLog.topic(getPresetTopic());
            eventLogHandler.handle(eventLog);
        });
    }

    default void fatal(@Nonnull Handler<KeelEventLog> eventLogHandler) {
        log(eventLog -> {
            eventLog.level(KeelLogLevel.FATAL);
            eventLog.topic(getPresetTopic());
            eventLogHandler.handle(eventLog);
        });
    }

//    default void log(KeelEventLog eventLog) {
//        getEventLogCenterSupplier().get().log(eventLog);
//    }

//    default void log(@Nonnull KeelLogLevel level, @Nonnull String msg) {
//        log(buildEventLog(level, new JsonObject()
//                .put(KeelEventLog.RESERVED_KEY_EVENT_MSG, msg)));
//    }

//    default void log(@Nonnull KeelLogLevel level, @Nonnull JsonObject event) {
//        log(buildEventLog(level, event));
//    }

//    default void debug(JsonObject event) {
//        log(KeelLogLevel.DEBUG, event);
//    }
//
//    default void info(JsonObject event) {
//        log(KeelLogLevel.INFO, event);
//    }
//
//    default void notice(JsonObject event) {
//        log(KeelLogLevel.NOTICE, event);
//    }
//
//    default void warning(JsonObject event) {
//        log(KeelLogLevel.WARNING, event);
//    }
//
//    default void error(JsonObject event) {
//        log(KeelLogLevel.ERROR, event);
//    }
//
//    default void fatal(JsonObject event) {
//        log(KeelLogLevel.FATAL, event);
//    }

    default void debug(String msg) {
        debug(eventLog -> eventLog.message(msg));
    }

    default void info(String msg) {
        info(eventLog -> eventLog.message(msg));
    }

    default void notice(String msg) {
        notice(eventLog -> eventLog.message(msg));
    }

    default void warning(String msg) {
        warning(eventLog -> eventLog.message(msg));
    }

    default void error(String msg) {
        error(eventLog -> eventLog.message(msg));
    }

    default void fatal(String msg) {
        fatal(eventLog -> eventLog.message(msg));
    }

    default Object processThrowable(@Nonnull Throwable throwable) {
        return getEventLogCenterSupplier().get().processThrowable(throwable);
    }

    default void exception(@Nonnull Throwable throwable) {
        exception(throwable, "Exception Occurred");
    }

    default void exception(@Nonnull Throwable throwable, @Nonnull String msg) {
        exception(throwable, eventLog -> {
            eventLog.message(msg);
        });
    }

    default void exception(@Nonnull Throwable throwable, @Nonnull Handler<KeelEventLog> eventLogHandler) {
        error(eventLog -> {
            eventLog.put(KeelEventLog.RESERVED_KEY_EVENT_EXCEPTION, this.processThrowable(throwable));
            eventLogHandler.handle(eventLog);
        });
    }

//    default void exception(@Nonnull Throwable throwable, JsonObject event) {
//        error(event
//                //.put(KeelEventLog.RESERVED_KEY_EVENT_MSG, "Exception" + " × " + throwable.getMessage())
//                .put(KeelEventLog.RESERVED_KEY_EVENT_EXCEPTION, processThrowable(throwable)));
//    }
}
