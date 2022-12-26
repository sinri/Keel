package io.github.sinri.keel.logger.event;

import java.util.function.Supplier;

public class KeelEventLoggerImpl implements KeelEventLogger {
    private final Supplier<KeelEventLogCenter> asyncEventLoggerSupplier;
    private final String presetTopic;

    public KeelEventLoggerImpl(String presetTopic, Supplier<KeelEventLogCenter> asyncEventLoggerSupplier) {
        this.presetTopic = presetTopic;
        this.asyncEventLoggerSupplier = asyncEventLoggerSupplier;
    }

    @Override
    public Supplier<KeelEventLogCenter> getEventLogCenterSupplier() {
        return asyncEventLoggerSupplier;
    }

    @Override
    public String getPresetTopic() {
        return presetTopic;
    }
}
