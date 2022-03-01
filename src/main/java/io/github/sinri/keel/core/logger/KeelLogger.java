package io.github.sinri.keel.core.logger;

import io.vertx.core.json.JsonObject;

/**
 * The Logger For Keel Project
 *
 * @since 1.11 Greatly Changed
 */
public class KeelLogger {

    protected KeelLoggerDelegate delegate;

    public KeelLogger(KeelLoggerOptions options) {
        this.delegate = new KeelLoggerDelegate(options);
    }

    public KeelLogger() {
        this.delegate = new KeelLoggerDelegate();
    }

    /**
     * @return a silent logger
     * @since 1.10
     */
    public static KeelLogger buildSilentLogger() {
        return new KeelLogger(new KeelLoggerOptions().setLowestLevel(KeelLogLevel.SILENT));
    }

    public KeelLogger setCategoryPrefix(String categoryPrefix) {
        this.delegate.setCategoryPrefix(categoryPrefix);
        return this;
    }

    public void debug(String msg, JsonObject context) {
        this.delegate.log(KeelLogLevel.DEBUG, msg, context);
    }

    public void debug(String msg) {
        this.delegate.log(KeelLogLevel.DEBUG, msg, null);
    }

    public void info(String msg, JsonObject context) {
        this.delegate.log(KeelLogLevel.INFO, msg, context);
    }

    public void info(String msg) {
        this.delegate.log(KeelLogLevel.INFO, msg, null);
    }

    public void notice(String msg, JsonObject context) {
        this.delegate.log(KeelLogLevel.NOTICE, msg, context);
    }

    public void notice(String msg) {
        this.delegate.log(KeelLogLevel.NOTICE, msg, null);
    }

    public void warning(String msg, JsonObject context) {
        this.delegate.log(KeelLogLevel.WARNING, msg, context);
    }

    public void warning(String msg) {
        this.delegate.log(KeelLogLevel.WARNING, msg, null);
    }

    public void error(String msg, JsonObject context) {
        this.delegate.log(KeelLogLevel.ERROR, msg, context);
    }

    public void error(String msg) {
        this.delegate.log(KeelLogLevel.ERROR, msg, null);
    }

    public void fatal(String msg, JsonObject context) {
        this.delegate.log(KeelLogLevel.FATAL, msg, context);
    }

    public void fatal(String msg) {
        this.delegate.log(KeelLogLevel.FATAL, msg, null);
    }

    public void exception(Throwable throwable) {
        exception(null, throwable);
    }

    /**
     * @param msg       since 1.10, a prefix String msg is supported
     * @param throwable the Throwable to print its details
     * @since 1.10
     */
    public void exception(String msg, Throwable throwable) {
        String prefix;
        if (msg == null || msg.isEmpty()) {
            prefix = "";
        } else {
            prefix = msg + " × ";
        }
        error(prefix + throwable.getMessage(), new JsonObject().put("error_class", throwable.getClass().getName()));
        for (var s : throwable.getStackTrace()) {
            this.delegate.print(KeelLogLevel.ERROR, "\t" + s.toString(), null);
        }
    }

    public void print(KeelLogLevel level, String content, String ending) {
        this.delegate.print(level, content, ending);
    }

    public void print(KeelLogLevel level, String content) {
        this.delegate.print(level, content, System.lineSeparator());
    }

    public void print(String content, String ending) {
        this.delegate.print(KeelLogLevel.INFO, content, ending);
    }

    public void print(String content) {
        this.delegate.print(KeelLogLevel.INFO, content, System.lineSeparator());
    }
}
