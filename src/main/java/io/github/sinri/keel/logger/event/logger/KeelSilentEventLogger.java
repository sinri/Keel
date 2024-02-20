package io.github.sinri.keel.logger.event.logger;

import io.github.sinri.keel.logger.KeelLogLevel;
import io.github.sinri.keel.logger.event.KeelEventLog;
import io.github.sinri.keel.logger.event.KeelEventLogCenter;
import io.github.sinri.keel.logger.event.KeelEventLogImpl;
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

    @Nonnull
    @Override
    public KeelLogLevel getVisibleLevel() {
        return KeelLogLevel.SILENT;
    }

    @Override
    public void setVisibleLevel(@Nonnull KeelLogLevel level) {

    }

    @Override
    public Supplier<KeelEventLogCenter> getEventLogCenterSupplier() {
        return KeelSilentEventLogCenter::getInstance;
    }

    @Override
    @Nonnull
    public String getPresetTopic() {
        return "";
    }

    @Nonnull
    @Override
    public Supplier<? extends KeelEventLog> getBaseLogBuilder() {
        return (Supplier<KeelEventLog>) () -> new KeelEventLogImpl(KeelLogLevel.SILENT, getPresetTopic());
    }

    @Override
    public void setBaseLogBuilder(@Nullable Supplier<? extends KeelEventLog> baseLogBuilder) {
        //
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
