package io.github.sinri.keel.logger.event;

import io.vertx.core.Future;
import io.vertx.core.Handler;

/**
 * @since 2.9.4 实验性设计
 */
public interface KeelEventLogCenter {

    void log(KeelEventLog eventLog);

    Object processThrowable(Throwable throwable);

    Future<Void> gracefullyClose();


    default KeelEventLogger createLogger(String presetTopic) {
        return createLogger(presetTopic, null);
    }

    default KeelEventLogger createLogger(String presetTopic, Handler<KeelEventLog> editor) {
        return new KeelEventLoggerImpl(presetTopic, () -> this, editor);
    }

}
