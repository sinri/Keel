package io.github.sinri.keel.logger.event;

import io.github.sinri.keel.logger.KeelLogLevel;
import io.github.sinri.keel.logger.issue.recorder.KeelIssueRecorder;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

/**
 * @since 3.2.0
 * The brand new KeelEventLogger based on KeelIssueRecorder.
 */
public interface KeelEventLogger {

    static KeelEventLogger from(@Nonnull KeelIssueRecorder<KeelEventLog> issueRecorder) {
        return from(issueRecorder, null);
    }

    static KeelEventLogger from(@Nonnull KeelIssueRecorder<KeelEventLog> issueRecorder, @Nullable Handler<KeelEventLog> templateEventLogEditor) {
        return new KeelEventLoggerImpl(issueRecorder, templateEventLogEditor);
    }

    @Nullable
    Handler<KeelEventLog> templateEventLogEditor();

    /**
     * @return Logs of this level or higher are visible.
     */
    @Nonnull
    KeelLogLevel getVisibleLevel();

    /**
     * @param level Logs of this level or higher are visible.
     */
    void setVisibleLevel(@Nonnull KeelLogLevel level);

    void addBypassLogger(@Nonnull KeelEventLogger bypassLogger);

    @Nonnull
    List<KeelEventLogger> getBypassLoggers();

    @Nonnull
    String getPresetTopic();

    void log(@Nonnull Handler<KeelEventLog> eventLogHandler);

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

    default void debug(@Nullable String msg) {
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
            if (context != null) eventLog.context(context);
        });
    }

    default void exception(@Nonnull Throwable throwable, @Nonnull Handler<KeelEventLog> eventLogHandler) {
        error(eventLog -> {
            eventLog.exception(throwable);
            eventLogHandler.handle(eventLog);
        });
    }

    /**
     * @since 3.0.1
     */
    default void debug(String msg, JsonObject context) {
        debug(event -> {
            event.message(msg);
            event.context(context);
        });
    }

    /**
     * @since 3.0.1
     */
    default void info(String msg, JsonObject context) {
        info(event -> {
            event.message(msg);
            event.context(context);
        });
    }

    /**
     * @since 3.0.1
     */
    default void notice(String msg, JsonObject context) {
        notice(event -> {
            event.message(msg);
            event.context(context);
        });
    }

    /**
     * @since 3.0.1
     */
    default void warning(String msg, JsonObject context) {
        warning(event -> {
            event.message(msg);
            event.context(context);
        });
    }

    /**
     * @since 3.0.1
     */
    default void error(String msg, JsonObject context) {
        error(event -> {
            event.message(msg);
            event.context(context);
        });
    }

    /**
     * @since 3.0.1
     */
    default void fatal(String msg, JsonObject context) {
        fatal(event -> {
            event.message(msg);
            event.context(context);
        });
    }

    /**
     * @since 3.1.10
     */
    default void debug(String msg, @Nonnull Handler<JsonObject> contextHandler) {
        debug(event -> {
            JsonObject context = new JsonObject();
            contextHandler.handle(context);
            event.message(msg);
            event.context(context);
        });
    }

    /**
     * @since 3.1.10
     */
    default void info(String msg, @Nonnull Handler<JsonObject> contextHandler) {
        info(event -> {
            JsonObject context = new JsonObject();
            contextHandler.handle(context);
            event.message(msg);
            event.context(context);
        });
    }

    /**
     * @since 3.1.10
     */
    default void notice(String msg, @Nonnull Handler<JsonObject> contextHandler) {
        notice(event -> {
            JsonObject context = new JsonObject();
            contextHandler.handle(context);
            event.message(msg);
            event.context(context);
        });
    }

    /**
     * @since 3.1.10
     */
    default void warning(String msg, @Nonnull Handler<JsonObject> contextHandler) {
        warning(event -> {
            JsonObject context = new JsonObject();
            contextHandler.handle(context);
            event.message(msg);
            event.context(context);
        });
    }

    /**
     * @since 3.1.10
     */
    default void error(String msg, @Nonnull Handler<JsonObject> contextHandler) {
        error(event -> {
            JsonObject context = new JsonObject();
            contextHandler.handle(context);
            event.message(msg);
            event.context(context);
        });
    }

    /**
     * @since 3.1.10
     */
    default void fatal(String msg, @Nonnull Handler<JsonObject> contextHandler) {
        fatal(event -> {
            JsonObject context = new JsonObject();
            contextHandler.handle(context);
            event.message(msg);
            event.context(context);
        });
    }

    /**
     * @since 3.1.10
     */
    default void exception(@Nonnull Throwable throwable, @Nonnull String msg, @Nonnull Handler<JsonObject> contextHandler) {
        exception(throwable, eventLog -> {
            JsonObject context = new JsonObject();
            contextHandler.handle(context);
            eventLog.message(msg);
            eventLog.context(context);
        });
    }
}
