package io.github.sinri.keel.logger.event;

import java.util.function.Supplier;

public class KeelEventLoggerImpl implements KeelEventLogger {
    private final Supplier<KeelEventLogCenter> eventLogCenterSupplier;
    private final String presetTopic;

    public KeelEventLoggerImpl(
            String presetTopic,
            Supplier<KeelEventLogCenter> eventLogCenterSupplier
    ) {
        this.presetTopic = presetTopic;
        this.eventLogCenterSupplier = eventLogCenterSupplier;
    }

    @Override
    public Supplier<KeelEventLogCenter> getEventLogCenterSupplier() {
        return eventLogCenterSupplier;
    }

    @Override
    public String getPresetTopic() {
        return presetTopic;
    }
}
