package io.github.sinri.keel.core.logger;

import io.vertx.core.json.JsonObject;

import java.util.HashSet;
import java.util.Set;

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
        exception(KeelLogLevel.ERROR, msg, throwable);
    }

    /**
     * @param level
     * @param msg
     * @param throwable
     * @since 2.1
     */
    public void exception(KeelLogLevel level, String msg, Throwable throwable) {
        if (level.isSilent() || level.isNegligibleThan(this.delegate.options.getLowestLevel())) {
            return;
        }
        if (throwable == null) {
            error(msg);
            return;
        }
        String prefix = throwable.getClass().getName() + " : " + throwable.getMessage();
        if (msg != null && !msg.isEmpty()) {
            prefix = msg + " × " + throwable.getClass().getName() + " : " + throwable.getMessage();
        }
        error(prefix);

        var lastThrowable = throwable;
        while (lastThrowable.getCause() != null) {
            var cause = lastThrowable.getCause();
            this.delegate.print(level, "Caused by " + cause.getClass().getName() + " : " + cause.getMessage(), null);
            lastThrowable = cause;
        }

        for (var s : lastThrowable.getStackTrace()) {
            this.delegate.print(level, "\t" + s.toString(), null);
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

    protected static Set<String> stackTraceClassIgnorePrefixSet = new HashSet<>();

    public static void registerStackTraceClassIgnorePrefix(String prefix) {
        stackTraceClassIgnorePrefixSet.add(prefix);
    }

    /**
     * 打印运行时调用栈
     *
     * @param remark Dying Message
     * @since 2.0
     */
    public void reportCurrentRuntimeCodeLocation(String remark) {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        StringBuilder sb = new StringBuilder("✟ " + remark);
        for (var ste : stackTrace) {
            boolean ignore = false;
            for (var prefix : stackTraceClassIgnorePrefixSet) {
                if (ste.getClassName().startsWith(prefix)) {
                    ignore = true;
                    break;
                }
            }
            if (ignore) continue;
            sb.append("↑ \t")
                    .append(ste.getClassName())
                    .append("::")
                    .append(ste.getMethodName())
                    .append(" (")
                    .append(ste.getFileName())
                    .append(":")
                    .append(ste.getLineNumber())
                    .append(")")
                    .append("\n");
        }
        print(sb.toString());
    }
}
