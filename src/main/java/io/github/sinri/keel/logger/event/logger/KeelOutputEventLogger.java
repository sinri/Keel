package io.github.sinri.keel.logger.event.logger;

import io.github.sinri.keel.logger.event.KeelEventLogCenter;
import io.github.sinri.keel.logger.event.center.KeelOutputEventLogCenter;

import java.util.function.Supplier;

public class KeelOutputEventLogger extends KeelFreeEventLogger {
    private final static KeelOutputEventLogger instance = new KeelOutputEventLogger();

    public static KeelOutputEventLogger getInstance() {
        return instance;
    }

    @Override
    public Supplier<KeelEventLogCenter> getEventLogCenterSupplier() {
        return KeelOutputEventLogCenter::getInstance;
    }

    @Override
    public String getPresetTopic() {
        return "";
    }
}
