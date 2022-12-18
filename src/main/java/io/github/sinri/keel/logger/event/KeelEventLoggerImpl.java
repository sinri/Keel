package io.github.sinri.keel.logger.event;

import io.github.sinri.keel.facade.Keel;

import java.util.function.Supplier;

public class KeelEventLoggerImpl implements KeelEventLogger {
    private final Supplier<KeelEventLogCenter> asyncEventLoggerSupplier;
    private final String presetTopic;
    private final Keel keel;

    public KeelEventLoggerImpl(Keel keel, String presetTopic, Supplier<KeelEventLogCenter> asyncEventLoggerSupplier) {
        this.keel = keel;
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

    @Override
    public Keel getKeel() {
        return keel;
    }
}
