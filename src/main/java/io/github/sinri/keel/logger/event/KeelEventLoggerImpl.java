package io.github.sinri.keel.logger.event;

import io.vertx.core.Handler;

import java.util.function.Supplier;

public class KeelEventLoggerImpl implements KeelEventLogger {
    private final Supplier<KeelEventLogCenter> eventLogCenterSupplier;
    private final String presetTopic;

    private Handler<KeelEventLog> presetEventLogEditor = null;

    public KeelEventLoggerImpl(
            String presetTopic,
            Supplier<KeelEventLogCenter> eventLogCenterSupplier
    ) {
        this(presetTopic, eventLogCenterSupplier, null);
    }

    @Override
    public Supplier<KeelEventLogCenter> getEventLogCenterSupplier() {
        return eventLogCenterSupplier;
    }

    @Override
    public String getPresetTopic() {
        return presetTopic;
    }

    public KeelEventLoggerImpl(
            String presetTopic,
            Supplier<KeelEventLogCenter> eventLogCenterSupplier,
            Handler<KeelEventLog> presetEventLogEditor
    ) {
        this.presetTopic = presetTopic;
        this.eventLogCenterSupplier = eventLogCenterSupplier;
        this.presetEventLogEditor = presetEventLogEditor;
    }

    @Override
    public Handler<KeelEventLog> getPresetEventLogEditor() {
        return presetEventLogEditor;
    }

    @Override
    public KeelEventLogger setPresetEventLogEditor(Handler<KeelEventLog> editor) {
        this.presetEventLogEditor = editor;
        return this;
    }
}
