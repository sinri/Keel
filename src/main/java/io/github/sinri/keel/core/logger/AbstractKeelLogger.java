package io.github.sinri.keel.core.logger;

import io.github.sinri.keel.Keel;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;

import java.util.Date;
import java.util.Objects;
import java.util.UUID;

abstract public class AbstractKeelLogger implements KeelLogger {
    private final String uniqueLoggerID;
    protected final KeelLoggerOptions options;
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
    public KeelLogger setContentPrefix(String prefix) {
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
        switch (this.options.getCompositionStyle()) {
            case ONE_JSON_OBJECT:
                return this.createTextFromLogAsOneJsonObject(level, msg, context);
            case ONE_LINE:
            case TWO_LINES:
            case THREE_LINES:
            default:
                return this.createTextFromLogAsLines(level, msg, context);
        }
    }

    private String createTextFromLogAsOneJsonObject(KeelLogLevel level, String msg, JsonObject context) {
        Date now = new Date();
        JsonObject x = new JsonObject();
        x.put("time", Keel.helpers().datetime().getDateExpression(now, "yyyy-MM-dd HH:mm:ss.SSS"));
        x.put("timestamp", now.getTime());

        String subject = options.getSubject();
        x.put("subject", subject);

        if (getCategoryPrefix() != null && !getCategoryPrefix().isEmpty()) {
            x.put("category", getCategoryPrefix());
        }

        if (this.options.shouldShowThreadID()) {
            var threadInfo = Thread.currentThread().getId();
            x.put("thread_id", threadInfo);
        }
        if (this.options.shouldShowVerticleDeploymentID()) {
            var verticleDeploymentInfo = Keel.getVertx().getOrCreateContext().deploymentID();
            x.put("verticle", verticleDeploymentInfo);
        }

        if (this.getContentPrefix().length() > 0) {
            x.put("prefix", this.getContentPrefix());
        }

        return x.put("message", msg).put("context", context).toString();
    }

    private String createTextFromLogAsLines(KeelLogLevel level, String msg, JsonObject context) {
        String content;
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

        String meta = Keel.helpers().datetime().getCurrentDateExpression("yyyy-MM-dd HH:mm:ss.SSS") + " "
                + "[" + level.name() + "] "
                + "<" + subject + "> "
                + threadInfo
                + verticleDeploymentInfo;
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

            if (this.options.getCompositionStyle() == KeelLoggerOptions.CompositionStyle.ONE_JSON_OBJECT) {
                error(prefix, Keel.helpers().json().renderThrowableChain(throwable, options.getIgnorableStackPackageSet()));
            } else {
                error(prefix, null);
                text(level, Keel.helpers().string().renderThrowableChain(throwable, options.getIgnorableStackPackageSet()), "");
            }
        }
    }

    @Override
    public void reportCurrentRuntimeCodeLocation(String remark) {
        if (this.isThisLevelVisible(KeelLogLevel.NOTICE)) {
            String text = Keel.helpers().datetime().getCurrentDateExpression("yyyy-MM-dd HH:mm:ss.SSS")
                    + " [REMARK] " + remark + System.lineSeparator()
                    + Keel.helpers().string().buildStackChainText(Thread.currentThread().getStackTrace(), options.getIgnorableStackPackageSet());
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

    @Override
    public void buffer(KeelLogLevel logLevel, boolean showAscii, Buffer buffer) {
        if (this.isThisLevelVisible(logLevel)) {
            String hexMatrix = Keel.helpers().string().bufferToHexMatrix(buffer, 32);
            if (showAscii) {
                hexMatrix += System.lineSeparator() + buffer.toString();
            }
            text(logLevel, hexMatrix, System.lineSeparator());
        }
    }
}
