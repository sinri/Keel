package io.github.sinri.keel.lagecy.core.logger.async;

import io.github.sinri.keel.lagecy.Keel;
import io.github.sinri.keel.lagecy.core.logger.KeelLogLevel;
import io.github.sinri.keel.lagecy.core.logger.LogMessage;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;

import java.util.Date;

/**
 * 将日志内容组装成 LogMessage 后加入指定队列。
 * *
 *
 * @since 2.9
 */
public class KeelAsyncMessageQueueLogger extends AbstractKeelAsyncQueueLogger<LogMessage> {

    private final AsyncLoggingServiceAdapter<LogMessage> loggingServiceAdapter;

    public KeelAsyncMessageQueueLogger(KeelAsyncQueueLoggerOptions options, AsyncLoggingServiceAdapter<LogMessage> loggingServiceAdapter) {
        super(options);
        this.loggingServiceAdapter = loggingServiceAdapter;
    }

    @Override
    protected Future<Void> dealWithBuffer() {
        if (this.loggingServiceAdapter == null) {
            return Future.failedFuture(new NullPointerException("loggingServiceAdapter is not set yet"));
        }
        return this.loggingServiceAdapter.dealWithLogs(this.getBuffer());
    }

    @Override
    protected LogMessage createLogObject(KeelLogLevel logLevel, String msg, JsonObject context) {
        String contentPrefix = getContentPrefix();
        if (contentPrefix == null || contentPrefix.isEmpty()) {
            contentPrefix = "";
        } else {
            contentPrefix = contentPrefix + " ";
        }
        LogMessage logMessage = new LogMessage(new Date().getTime(), logLevel, this.options.getAspect(), contentPrefix + msg, getCategoryPrefix(), context);
        if (this.options.shouldShowThreadID()) {
            logMessage.setLogThread(Thread.currentThread().getId());
        }
        if (this.options.shouldShowVerticleDeploymentID()) {
            logMessage.setLogVerticle(Keel.getVertx().getOrCreateContext().deploymentID());
        }

        return logMessage;
    }

    @Override
    protected LogMessage createLogObject(String rawMessage) {
        return createLogObject(KeelLogLevel.INFO, rawMessage, null);
    }

//    @Override
//    protected void printLog(KeelLogLevel logLevel, String message, JsonObject context) {
//        String oneLineTextForLog = createOneLineTextForLog(logLevel, message, context);
//        System.out.println(oneLineTextForLog);
//    }

}
