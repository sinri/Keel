package io.github.sinri.keel.logger.event.logger;

import io.github.sinri.keel.logger.event.KeelEventLog;
import io.github.sinri.keel.logger.event.KeelEventLogCenter;
import io.github.sinri.keel.logger.event.KeelEventLogger;
import io.github.sinri.keel.logger.event.center.KeelSilentEventLogCenter;
import io.vertx.core.Handler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Supplier;

public class KeelSilentEventLogger implements KeelEventLogger {
    private final static KeelSilentEventLogger instance = new KeelSilentEventLogger();

    public static KeelSilentEventLogger getInstance() {
        return instance;
    }

    @Override
    public Supplier<KeelEventLogCenter> getEventLogCenterSupplier() {
        return KeelSilentEventLogCenter::getInstance;
    }

    @Override
    public String getPresetTopic() {
        return null;
    }

    @Override
    @Nullable
    public Handler<KeelEventLog> getPresetEventLogEditor() {
        return null;
    }

    @Override
    public KeelEventLogger setPresetEventLogEditor(@Nullable Handler<KeelEventLog> editor) {
        return this;
    }

    @Override
    public void addBypassLogger(@Nonnull KeelEventLogger bypassLogger) {

    }

    @Nonnull
    @Override
    public List<KeelEventLogger> getBypassLoggers() {
        return List.of();
    }

    @Override
    public void log(@Nonnull Handler<KeelEventLog> eventLogHandler) {
        // keep silent
    }
}
