package io.github.sinri.keel.logger.event;

import io.github.sinri.keel.facade.Keel;
import io.vertx.core.Future;

/**
 * @since 2.9.4 实验性设计
 */
public interface KeelEventLogCenter {

    Keel getKeel();

    void log(KeelEventLog eventLog);

    Object processThrowable(Throwable throwable);

    Future<Void> gracefullyClose();

    default KeelEventLogger createLogger(String presetTopic) {
        return new KeelEventLoggerImpl(getKeel(), presetTopic, () -> this);
    }

}
