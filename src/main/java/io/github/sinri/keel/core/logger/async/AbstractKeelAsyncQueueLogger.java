package io.github.sinri.keel.core.logger.async;

import io.github.sinri.keel.Keel;
import io.github.sinri.keel.core.logger.KeelLogLevel;
import io.github.sinri.keel.core.logger.KeelLogger;
import io.vertx.core.Future;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * 异步日志器。
 * 目前可以预见的场景，主要在SAE容器环境下使用，配置不涉密，且与本地（容器内）环境无关，完全可以页面写死。
 *
 * @since 2.9
 */
abstract public class AbstractKeelAsyncQueueLogger<T> implements KeelLogger {
    protected final KeelAsyncQueueLoggerOptions options;
    private final Queue<T> queue;
    private final List<T> buffer;
    private final String uniqueLoggerID;
    protected String categoryPrefix;
    protected String contentPrefix;
    private int batchSize = 128;
    private long sleep = 128L;
    private long maxSleep = 2048L;

    public AbstractKeelAsyncQueueLogger(KeelAsyncQueueLoggerOptions options) {
        this.options = options;
        this.uniqueLoggerID = UUID.randomUUID().toString();
        this.queue = new ConcurrentLinkedQueue<>();
        this.buffer = new ArrayList<>();
        routine();
    }

    public AbstractKeelAsyncQueueLogger<T> setBatchSize(int batchSize) {
        this.batchSize = batchSize;
        return this;
    }

    public AbstractKeelAsyncQueueLogger<T> setMaxSleep(long maxSleep) {
        this.maxSleep = maxSleep;
        return this;
    }

    protected void enqueue(T t) {
        this.queue.offer(t);
        if (this.options.isUseTee()) {
            System.out.println(t.toString());
        }
    }

    abstract protected T createLogObject(KeelLogLevel logLevel, String message, JsonObject context);

    abstract protected T createLogObject(String rawMessage);

//    protected final void printLog(String rawMessage) {
//        System.out.println(rawMessage);
//    }
//
//    abstract protected void printLog(KeelLogLevel logLevel, String message, JsonObject context);

    protected synchronized void routine() {
        // 1. Clean the buffer and read a batch of logs to buffer
        while (buffer.size() < batchSize) {
            var x = this.queue.poll();
            if (x == null) {
                break;
            }
            buffer.add(x);
        }

        // 2. write buffer to target
        dealWithBuffer().andThen(ar -> {
            if (ar.failed()) {
                sleep = 1024L;
            } else {
                buffer.clear();
                if (this.queue.isEmpty()) {
                    sleep = Math.min(sleep * 2, maxSleep);
                } else {
                    sleep = 1L;
                }
            }
            Keel.getVertx().setTimer(sleep, timerID -> routine());
        });
    }

    protected List<T> getBuffer() {
        return buffer;
    }

    /**
     * IF buffer is empty: return success future.
     * ELSE:
     * save all the logs in buffer in order and remove the saved ones.
     */
    abstract protected Future<Void> dealWithBuffer();

    @Override
    public String getUniqueLoggerID() {
        return this.uniqueLoggerID;
    }

    @Override
    public KeelLogger setCategoryPrefix(String categoryPrefix) {
        this.categoryPrefix = categoryPrefix;
        return this;
    }

    @Override
    public KeelLogger setContentPrefix(String prefix) {
        this.contentPrefix = prefix;
        return this;
    }

    @Override
    public void debug(String msg, JsonObject context) {
        if (this.isThisLevelVisible(KeelLogLevel.DEBUG)) {
            T t = createLogObject(KeelLogLevel.DEBUG, msg, context);
            this.enqueue(t);
        }
    }

    @Override
    public void info(String msg, JsonObject context) {
        if (this.isThisLevelVisible(KeelLogLevel.INFO)) {
            T t = createLogObject(KeelLogLevel.INFO, msg, context);
            this.enqueue(t);
        }
    }

    @Override
    public void notice(String msg, JsonObject context) {
        if (this.isThisLevelVisible(KeelLogLevel.NOTICE)) {
            T t = createLogObject(KeelLogLevel.NOTICE, msg, context);
            this.enqueue(t);
        }
    }

    @Override
    public void warning(String msg, JsonObject context) {
        if (this.isThisLevelVisible(KeelLogLevel.WARNING)) {
            T t = createLogObject(KeelLogLevel.WARNING, msg, context);
            this.enqueue(t);
        }
    }

    @Override
    public void error(String msg, JsonObject context) {
        if (this.isThisLevelVisible(KeelLogLevel.ERROR)) {
            T t = createLogObject(KeelLogLevel.ERROR, msg, context);
            this.enqueue(t);
        }
    }

    @Override
    public void fatal(String msg, JsonObject context) {
        if (this.isThisLevelVisible(KeelLogLevel.FATAL)) {
            T t = createLogObject(KeelLogLevel.FATAL, msg, context);
            this.enqueue(t);
        }
    }

    @Override
    public void exception(KeelLogLevel level, String msg, Throwable throwable) {
        if (this.isThisLevelVisible(KeelLogLevel.ERROR)) {
            String throwableChain = Keel.helpers().string().renderThrowableChain(throwable);
            T t = createLogObject(KeelLogLevel.ERROR, msg, new JsonObject()
                    .put("error", throwableChain)
            );
            this.enqueue(t);
        }
    }

    @Override
    public void text(String text) {
        text(KeelLogLevel.INFO, text, System.lineSeparator());
    }

    @Override
    public void text(String text, String lineEnding) {
        text(KeelLogLevel.INFO, text, lineEnding);
    }

    @Override
    public void text(KeelLogLevel logLevel, String text, String lineEnding) {
        if (this.isThisLevelVisible(logLevel)) {
            T t = createLogObject(text);
            this.enqueue(t);
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
    public void buffer(KeelLogLevel logLevel, boolean showAscii, Buffer buffer) {
        if (this.isThisLevelVisible(logLevel)) {
            String hexMatrix = Keel.helpers().string().bufferToHexMatrix(buffer, 32);
            if (showAscii) {
                hexMatrix += System.lineSeparator() + buffer.toString();
            }
            text(logLevel, hexMatrix, System.lineSeparator());
        }
    }

    public final boolean isThisLevelVisible(KeelLogLevel logLevel) {
        KeelLogLevel lowestVisibleLogLevel = options.getLowestVisibleLogLevel();
        if (lowestVisibleLogLevel == null) {
            lowestVisibleLogLevel = KeelLogLevel.INFO;
        }
        return logLevel.isEnoughSeriousAs(lowestVisibleLogLevel);
    }

    public String getContentPrefix() {
        return contentPrefix;
    }

    public String getCategoryPrefix() {
        return categoryPrefix;
    }

    protected final String createOneLineTextForLog(KeelLogLevel level, String msg, JsonObject context) {
        String content;
        String subject = options.getAspect();
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

        // as ONE_LINE
        content = meta;
        if (this.getContentPrefix().length() > 0) {
            content += this.getContentPrefix() + " ";
        }
        content += msg;
        if (context != null) {
            content += " | " + context;
        }
        return content;
    }

}
