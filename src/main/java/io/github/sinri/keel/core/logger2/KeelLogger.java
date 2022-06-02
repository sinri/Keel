package io.github.sinri.keel.core.logger2;

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
        } catch (ClassCastException | InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException | ClassNotFoundException e) {
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
    void setCategoryPrefix(String categoryPrefix);

    void debug(String msg);

    void debug(String msg, JsonObject context);

    void info(String msg);

    void info(String msg, JsonObject context);

    void notice(String msg);

    void notice(String msg, JsonObject context);

    void warning(String msg);

    void warning(String msg, JsonObject context);

    void error(String msg);

    void error(String msg, JsonObject context);

    void fatal(String msg);

    void fatal(String msg, JsonObject context);

    void exception(Throwable throwable);

    void exception(String msg, Throwable throwable);

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
}
