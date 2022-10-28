package io.github.sinri.keel.core.logger.async;

import io.github.sinri.keel.core.logger.KeelLogLevel;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;

/**
 * 将日志内容组装成String后加入指定队列。
 *
 * @since 2.9
 */
public class KeelAsyncSimpleQueueLogger extends AbstractKeelAsyncQueueLogger<String> {

    private final AsyncLoggingServiceAdapter<String> asyncLoggingServiceAdapter;

    public KeelAsyncSimpleQueueLogger(KeelAsyncQueueLoggerOptions options, AsyncLoggingServiceAdapter<String> asyncLoggingServiceAdapter) {
        super(options);
        this.asyncLoggingServiceAdapter = asyncLoggingServiceAdapter;
    }

    @Override
    protected String createLogObject(KeelLogLevel logLevel, String message, JsonObject context) {
        return createOneLineTextForLog(logLevel, message, context);
    }

    @Override
    protected String createLogObject(String rawMessage) {
        return rawMessage;
    }

//    @Override
//    protected void printLog(KeelLogLevel logLevel, String message, JsonObject context) {
//        String s = createLogObject(logLevel, message, context);
//        printLog(s);
//    }

    @Override
    protected Future<Void> dealWithBuffer() {
        return asyncLoggingServiceAdapter.dealWithLogs(this.getBuffer());
    }


}
