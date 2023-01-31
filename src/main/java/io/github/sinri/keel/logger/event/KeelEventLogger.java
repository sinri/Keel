package io.github.sinri.keel.logger.event;

import io.github.sinri.keel.logger.KeelLogLevel;
import io.github.sinri.keel.logger.event.logger.KeelSilentEventLogger;
import io.vertx.core.Future;
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


    default void log(@Nonnull Handler<KeelEventLog> eventLogHandler) {
        // done debugging
//        System.out.println("KeelEventLogger::log("+eventLogHandler+") start");

        KeelEventLog eventLog = KeelEventLog.create(KeelLogLevel.INFO, getPresetTopic());

//        System.out.println("KeelEventLogger::log("+eventLogHandler+") eventLog created");

        Handler<KeelEventLog> presetEventLogEditor = getPresetEventLogEditor();
        if (presetEventLogEditor != null) {
//            System.out.println("KeelEventLogger::log("+eventLogHandler+") presetEventLogEditor is not null");
            presetEventLogEditor.handle(eventLog);
        } else {
//            System.out.println("KeelEventLogger::log("+eventLogHandler+") presetEventLogEditor is null");
        }

//        System.out.println("KeelEventLogger::log("+eventLogHandler+") presetEventLogEditor done");

        eventLogHandler.handle(eventLog);

//        System.out.println("KeelEventLogger::log("+eventLogHandler+") eventLogHandler done");

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
