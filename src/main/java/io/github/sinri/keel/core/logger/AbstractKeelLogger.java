package io.github.sinri.keel.core.logger;

import io.github.sinri.keel.Keel;
import io.vertx.core.json.JsonObject;

import java.util.Objects;
import java.util.UUID;

abstract public class AbstractKeelLogger implements KeelLogger {
    private final String uniqueLoggerID;
    protected KeelLoggerOptions options;
    private String categoryPrefix;
    private String contentPrefix = "";

    public AbstractKeelLogger(KeelLoggerOptions options) {
        this.options = options;
        this.uniqueLoggerID = UUID.randomUUID().toString();
    }

    public final boolean isThisLevelVisible(KeelLogLevel logLevel) {
        KeelLogLevel lowestVisibleLogLevel = options.getLowestVisibleLogLevel();
        if (lowestVisibleLogLevel == null) {
            lowestVisibleLogLevel = KeelLogLevel.INFO;
        }
        return logLevel.isEnoughSeriousAs(lowestVisibleLogLevel);
    }

    protected String getCategoryPrefix() {
        return this.categoryPrefix;
    }

    @Override
    public KeelLogger setCategoryPrefix(String categoryPrefix) {
        this.categoryPrefix = categoryPrefix;
        return this;
    }

    /**
     * @since 2.8
     */
    @Override
    public KeelLogger setContextPrefix(String prefix) {
        this.contentPrefix = Objects.requireNonNullElse(prefix, "");
        return this;
    }

    /**
     * @since 2.8
     */
    protected String getContentPrefix() {
        return Objects.requireNonNullElse(this.contentPrefix, "");
    }

    protected String createTextFromLog(KeelLogLevel level, String msg, JsonObject context) {
        String subject = options.getSubject();
        if (getCategoryPrefix() != null && !getCategoryPrefix().isEmpty()) {
            subject += ":" + getCategoryPrefix();
        }

        String threadInfo = "";
        if (this.options.shouldShowThreadID()) {
            threadInfo = "[" + Thread.currentThread().getId() + "] ";
        }
        String verticleDeploymentInfo = "";
        if (this.options.shouldShowVerticleDeploymentID()) {
            verticleDeploymentInfo = "{" + Keel.getVertx().getOrCreateContext().deploymentID() + "} ";
        }

        String meta = Keel.dateTimeHelper().getCurrentDateExpression("yyyy-MM-dd HH:mm:ss.SSS") + " "
                + "[" + level.name() + "] "
                + "<" + subject + "> "
                + threadInfo
                + verticleDeploymentInfo;

        String content;
        if (this.options.getCompositionStyle() == KeelLoggerOptions.CompositionStyle.TWO_LINES) {
            content = meta + System.lineSeparator();
            if (this.getContentPrefix().length() > 0) {
                content += this.getContentPrefix() + " ";
            }
            content += msg;
            if (context != null) {
                content += " | " + context;
            }
        } else if (this.options.getCompositionStyle() == KeelLoggerOptions.CompositionStyle.THREE_LINES) {
            content = meta + System.lineSeparator();
            if (this.getContentPrefix().length() > 0) {
                content += this.getContentPrefix() + " ";
            }
            content += msg;
            if (context != null) {
                content += System.lineSeparator() + context;
            }
        } else {
            // as ONE_LINE
            content = meta;
            if (this.getContentPrefix().length() > 0) {
                content += this.getContentPrefix() + " ";
            }
            content += msg;
            if (context != null) {
                content += " | " + context;
            }
        }

        return content;
    }

    protected void writeLog(KeelLogLevel logLevel, String msg, JsonObject context) {
        String textFromLog = createTextFromLog(logLevel, msg, context);
        text(logLevel, textFromLog, null);
    }

    @Override
    public String getUniqueLoggerID() {
        return uniqueLoggerID;
    }

    @Override
    public void debug(String msg, JsonObject context) {
        if (this.isThisLevelVisible(KeelLogLevel.DEBUG)) {
            writeLog(KeelLogLevel.DEBUG, msg, context);
        }
    }

    @Override
    public void info(String msg, JsonObject context) {
        if (this.isThisLevelVisible(KeelLogLevel.INFO)) {
            writeLog(KeelLogLevel.INFO, msg, context);
        }
    }

    @Override
    public void notice(String msg, JsonObject context) {
        if (this.isThisLevelVisible(KeelLogLevel.NOTICE)) {
            writeLog(KeelLogLevel.NOTICE, msg, context);
        }
    }

    @Override
    public void warning(String msg, JsonObject context) {
        if (this.isThisLevelVisible(KeelLogLevel.WARNING)) {
            writeLog(KeelLogLevel.WARNING, msg, context);
        }
    }

    @Override
    public void error(String msg, JsonObject context) {
        if (this.isThisLevelVisible(KeelLogLevel.ERROR)) {
            writeLog(KeelLogLevel.ERROR, msg, context);
        }
    }

    @Override
    public void fatal(String msg, JsonObject context) {
        if (this.isThisLevelVisible(KeelLogLevel.FATAL)) {
            writeLog(KeelLogLevel.FATAL, msg, context);
        }
    }

    @Override
    public void exception(KeelLogLevel level, String msg, Throwable throwable) {
        if (this.isThisLevelVisible(level)) {
            if (throwable == null) {
                error(msg);
                return;
            }

            String prefix = throwable.getClass().getName() + " : " + throwable.getMessage();
            if (msg != null && !msg.isEmpty()) {
                prefix = msg + " × " + throwable.getClass().getName() + " : " + throwable.getMessage();
            }
            error(prefix, null);
            text(level, renderThrowableChain(throwable), "");
        }
    }

    private String buildStackChainText(StackTraceElement[] stackTrace) {
        StringBuilder sb = new StringBuilder();
        if (stackTrace != null) {
            String ignoringClassPackage = null;
            int ignoringCount = 0;
            for (StackTraceElement stackTranceItem : stackTrace) {
                String className = stackTranceItem.getClassName();
                String matchedClassPackage = this.options.matchIgnorableStackPackage(className);
                if (matchedClassPackage == null) {
                    if (ignoringCount > 0) {
                        sb.append("\t\t")
                                .append("[").append(ignoringCount).append("] ")
                                .append(ignoringClassPackage)
                                .append(System.lineSeparator());

                        ignoringClassPackage = null;
                        ignoringCount = 0;
                    }

                    sb.append("\t\t")
                            .append(stackTranceItem.getClassName())
                            .append(".")
                            .append(stackTranceItem.getMethodName())
                            .append(" (")
                            .append(stackTranceItem.getFileName())
                            .append(":")
                            .append(stackTranceItem.getLineNumber())
                            .append(")")
                            .append(System.lineSeparator());
                } else {
                    if (ignoringCount > 0) {
                        if (ignoringClassPackage.equals(matchedClassPackage)) {
                            ignoringCount += 1;
                        } else {
                            sb.append("\t\t")
                                    .append("[").append(ignoringCount).append("] ")
                                    .append(ignoringClassPackage)
                                    .append(System.lineSeparator());

                            ignoringClassPackage = matchedClassPackage;
                            ignoringCount = 1;
                        }
                    } else {
                        ignoringClassPackage = matchedClassPackage;
                        ignoringCount = 1;
                    }
                }
            }
            if (ignoringCount > 0) {
                sb.append("\t\t")
                        .append("[").append(ignoringCount).append("] ")
                        .append(ignoringClassPackage)
                        .append(System.lineSeparator());
            }
        }
        return sb.toString();
    }

    private String renderThrowableChain(Throwable throwable) {
        if (throwable == null) return "";
        Throwable cause = throwable.getCause();
        StringBuilder sb = new StringBuilder();
        sb
                .append("\t")
                .append(throwable.getClass().getName())
                .append(": ")
                .append(throwable.getMessage())
                .append(System.lineSeparator())
                .append(buildStackChainText(throwable.getStackTrace()));

        while (cause != null) {
            sb
                    .append("\t↑ ")
                    .append(cause.getClass().getName())
                    .append(": ")
                    .append(cause.getMessage())
                    .append(System.lineSeparator())
                    .append(buildStackChainText(cause.getStackTrace()))
            ;

            cause = cause.getCause();
        }

        return sb.toString();
    }

    @Override
    public void reportCurrentRuntimeCodeLocation(String remark) {
        if (this.isThisLevelVisible(KeelLogLevel.NOTICE)) {
            String text = Keel.dateTimeHelper().getCurrentDateExpression("yyyy-MM-dd HH:mm:ss.SSS") + " [REMARK] " + remark + System.lineSeparator() + buildStackChainText(Thread.currentThread().getStackTrace());
            text(text);
        }
    }

    @Override
    public void text(String text, String lineEnding) {
        if (lineEnding == null) {
            text(text + System.lineSeparator());
        } else {
            text(text + lineEnding);
        }
    }

    @Override
    public void text(KeelLogLevel logLevel, String text, String lineEnding) {
        if (this.isThisLevelVisible(logLevel)) {
            text(text, lineEnding);
        }
    }
}
