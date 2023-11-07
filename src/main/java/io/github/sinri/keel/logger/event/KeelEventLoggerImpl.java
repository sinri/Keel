package io.github.sinri.keel.logger.event;

import io.vertx.core.Handler;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
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
    public Supplier<KeelEventLogCenter> getEventLogCenterSupplier() {
        return eventLogCenterSupplier;
    }

    @Override
    public String getPresetTopic() {
        return presetTopic;
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

    /**
     * @since 3.0.10
     */
    private final List<KeelEventLogger> bypassLoggerList = new ArrayList<>();

    /**
     * @since 3.0.10
     */
    @Override
    public void addBypassLogger(KeelEventLogger bypassLogger) {
        this.bypassLoggerList.add(bypassLogger);
    }

    /**
     * @since 3.0.10
     */
    @NotNull
    @Override
    public List<KeelEventLogger> getBypassLoggers() {
        return this.bypassLoggerList;
    }
}
