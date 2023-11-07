package io.github.sinri.keel.logger.event;

import io.github.sinri.keel.logger.KeelLogLevel;
import io.github.sinri.keel.logger.event.logger.KeelSilentEventLogger;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.function.Supplier;

/**
 * @since 2.9.4
 * @since 3.0.10 Add Bypass Loggers Support.
 */
public interface KeelEventLogger {
    static KeelEventLogger silentLogger() {
        return KeelSilentEventLogger.getInstance();
    }

    /**
     * Note: it is better to keep log center stable.
     */
    Supplier<KeelEventLogCenter> getEventLogCenterSupplier();

    /**
     * Note: if `getEventLogCenterSupplier` generate instances dynamically, the default implement would not affect.
     */
    default Future<Void> gracefullyCloseLogCenter() {
        return getEventLogCenterSupplier().get().gracefullyClose();
    }

    String getPresetTopic();

    Handler<KeelEventLog> getPresetEventLogEditor();

    KeelEventLogger setPresetEventLogEditor(Handler<KeelEventLog> editor);

    /**
     * @since 3.0.10
     * Add a bypass logger to this logger.
     */
    void addBypassLogger(KeelEventLogger bypassLogger);

    /**
     * @since 3.0.10
     * Get the registered bypass loggers, which would be called to handle any logs received by this logger.
     */
    @Nonnull
    List<KeelEventLogger> getBypassLoggers();

    default void log(@Nonnull Handler<KeelEventLog> eventLogHandler) {
        KeelEventLog eventLog = KeelEventLog.create(KeelLogLevel.INFO, getPresetTopic());
        Handler<KeelEventLog> presetEventLogEditor = getPresetEventLogEditor();
        if (presetEventLogEditor != null) {
            presetEventLogEditor.handle(eventLog);
        }
        eventLogHandler.handle(eventLog);
        getEventLogCenterSupplier().get().log(eventLog);

        // since 3.0.10, log to registered bypass loggers.
        getBypassLoggers().forEach(bypassLogger -> bypassLogger.log(eventLogHandler));
    }

    default void debug(@Nonnull Handler<KeelEventLog> eventLogHandler) {
        log(eventLog -> {
            eventLog.level(KeelLogLevel.DEBUG);
            eventLog.topic(getPresetTopic());
            eventLogHandler.handle(eventLog);
        });
    }

    default void info(@Nonnull Handler<KeelEventLog> eventLogHandler) {
        // done debugging
//        System.out.println("KeelEventLogger::info("+eventLogHandler+") start");
        log(eventLog -> {
//            System.out.println("KeelEventLogger::info("+eventLogHandler+") inside handler start");
            eventLog.level(KeelLogLevel.INFO);
            eventLog.topic(getPresetTopic());
//            System.out.println("KeelEventLogger::info("+eventLogHandler+") inside handler go");
            eventLogHandler.handle(eventLog);
//            System.out.println("KeelEventLogger::info("+eventLogHandler+") inside handler gone");
        });
//        System.out.println("KeelEventLogger::info("+eventLogHandler+") end");
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

    default void debug(String msg) {
        debug(eventLog -> eventLog.message(msg));
    }

    default void info(String msg) {
        // done debugging
//        System.out.println("KeelEventLogger::info("+msg+") start");
        info(eventLog -> eventLog.message(msg));
//        System.out.println("KeelEventLogger::info("+msg+") end");
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

    /**
     * @since 3.0.1
     */
    default void exception(@Nonnull Throwable throwable, @Nonnull String msg, @Nullable JsonObject context) {
        exception(throwable, eventLog -> {
            eventLog.message(msg);
            if (context != null) eventLog.put("context", context);
        });
    }

    default void exception(@Nonnull Throwable throwable, @Nonnull Handler<KeelEventLog> eventLogHandler) {
        error(eventLog -> {
            eventLog.put(KeelEventLog.RESERVED_KEY_EVENT_EXCEPTION, this.processThrowable(throwable));
            eventLogHandler.handle(eventLog);
        });
    }

    /**
     * @since 3.0.1
     */
    default void debug(String msg, JsonObject context) {
        debug(event -> {
            event.message(msg);
            if (context != null) event.put("context", context);
        });
    }

    /**
     * @since 3.0.1
     */
    default void info(String msg, JsonObject context) {
        info(event -> {
            event.message(msg);
            if (context != null) event.put("context", context);
        });
    }

    /**
     * @since 3.0.1
     */
    default void notice(String msg, JsonObject context) {
        notice(event -> {
            event.message(msg);
            if (context != null) event.put("context", context);
        });
    }

    /**
     * @since 3.0.1
     */
    default void warning(String msg, JsonObject context) {
        warning(event -> {
            event.message(msg);
            if (context != null) event.put("context", context);
        });
    }

    /**
     * @since 3.0.1
     */
    default void error(String msg, JsonObject context) {
        error(event -> {
            event.message(msg);
            if (context != null) event.put("context", context);
        });
    }

    /**
     * @since 3.0.1
     */
    default void fatal(String msg, JsonObject context) {
        fatal(event -> {
            event.message(msg);
            if (context != null) event.put("context", context);
        });
    }
}
