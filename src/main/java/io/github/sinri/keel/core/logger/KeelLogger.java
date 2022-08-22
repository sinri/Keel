package io.github.sinri.keel.core.logger;

import io.github.sinri.keel.core.logger.impl.KeelPrintLogger;
import io.github.sinri.keel.core.logger.impl.KeelSilentLogger;
import io.github.sinri.keel.core.logger.impl.KeelSyncFileLogger;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;

import javax.annotation.Nonnull;
import java.lang.reflect.InvocationTargetException;

/**
 * @since 2.6
 */
public interface KeelLogger {
    /**
     * Create an instance of KeelLogger according to given KeelLoggerOptions.
     */
    static KeelLogger createLogger(@Nonnull KeelLoggerOptions keelLoggerOptions) {
        if (keelLoggerOptions.getImplement() == null || keelLoggerOptions.getImplement().isEmpty()) {
            return new KeelSyncFileLogger(keelLoggerOptions);
        }
        if (keelLoggerOptions.getImplement().equals("sync")) {
            return new KeelSyncFileLogger(keelLoggerOptions);
        }
//        if (keelLoggerOptions.implement.equals("async")) {
//            return new KeelAsyncFileLogger(keelLoggerOptions);
//        }
        if (keelLoggerOptions.getImplement().equals("silent")) {
            return silentLogger();
        }
        if (keelLoggerOptions.getImplement().equals("print")) {
            return new KeelPrintLogger(keelLoggerOptions);
        }

        // treat implement as customized implement class
        try {
            return (KeelLogger) Class.forName(keelLoggerOptions.getImplement())
                    .getConstructor(KeelLoggerOptions.class)
                    .newInstance(keelLoggerOptions);
        } catch (ClassCastException | InstantiationException | IllegalAccessException | InvocationTargetException |
                 NoSuchMethodException | ClassNotFoundException e) {
            e.printStackTrace();
            // deal with printLogger()
            return new KeelPrintLogger(keelLoggerOptions);
        }
    }

    /**
     * @return the singleton of KeelSilentLogger
     */
    static KeelLogger silentLogger() {
        return KeelSilentLogger.getInstance();
    }

    /**
     * @return the unique logger ID.
     */
    String getUniqueLoggerID();

    /**
     * Under one subject (aspect), there might be several categories.
     *
     * @param categoryPrefix the category prefix string
     */
    KeelLogger setCategoryPrefix(String categoryPrefix);

    /**
     * 设置在标准的日志级别日志内容中固定添加的前缀文字
     *
     * @since 2.8
     */
    KeelLogger setContentPrefix(String prefix);

    default void debug(String msg) {
        debug(msg, null);
    }

    void debug(String msg, JsonObject context);

    default void info(String msg) {
        info(msg, null);
    }

    void info(String msg, JsonObject context);

    default void notice(String msg) {
        notice(msg, null);
    }

    void notice(String msg, JsonObject context);

    default void warning(String msg) {
        warning(msg, null);
    }

    void warning(String msg, JsonObject context);

    default void error(String msg) {
        error(msg, null);
    }

    void error(String msg, JsonObject context);

    default void fatal(String msg) {
        fatal(msg, null);
    }

    void fatal(String msg, JsonObject context);

    default void exception(Throwable throwable) {
        exception(KeelLogLevel.ERROR, null, throwable);
    }

    default void exception(String msg, Throwable throwable) {
        exception(KeelLogLevel.ERROR, msg, throwable);
    }

    void exception(KeelLogLevel level, String msg, Throwable throwable);

    /**
     * @param text the final text to be rendered to target display; would append nothing to it anymore.
     */
    void text(String text);

    /**
     * @param text       the final text to be rendered to target display
     * @param lineEnding Certain string to be appended to the ending of text; if null, would be treated as system defined line separator.
     */
    void text(String text, String lineEnding);

    /**
     * @param logLevel   the log level
     * @param text       the final text to be rendered to target display
     * @param lineEnding Certain string to be appended to the ending of text; if null, would be treated as system defined line separator.
     */
    void text(KeelLogLevel logLevel, String text, String lineEnding);

    void reportCurrentRuntimeCodeLocation(String remark);

    /**
     * @since 2.8
     */
    default void buffer(Buffer buffer) {
        buffer(KeelLogLevel.DEBUG, true, buffer);
    }

    /**
     * @since 2.8
     */
    void buffer(KeelLogLevel logLevel, boolean showAscii, Buffer buffer);
}
