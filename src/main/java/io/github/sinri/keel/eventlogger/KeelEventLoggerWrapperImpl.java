package io.github.sinri.keel.eventlogger;

import java.util.function.Supplier;

public class KeelEventLoggerWrapperImpl implements KeelEventLoggerWrapper {
    private final Supplier<KeelEventLogger> asyncEventLoggerSupplier;
    private final String presetTopic;

    public KeelEventLoggerWrapperImpl(String presetTopic, Supplier<KeelEventLogger> asyncEventLoggerSupplier) {
        this.presetTopic = presetTopic;
        this.asyncEventLoggerSupplier = asyncEventLoggerSupplier;
    }

    @Override
    public Supplier<KeelEventLogger> getEventLoggerSupplier() {
        return asyncEventLoggerSupplier;
    }

    @Override
    public String getPresetTopic() {
        return presetTopic;
    }
}
