package io.github.sinri.keel.logger.event.legacy;

import io.github.sinri.keel.logger.KeelLogLevel;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

@Deprecated(since = "3.2.0", forRemoval = true)
public class KeelEventLoggerImpl implements KeelEventLogger {
    private final @Nonnull Supplier<KeelEventLogCenter> eventLogCenterSupplier;
    private final @Nonnull String presetTopic;
    private KeelLogLevel visibleLogLevel = KeelLogLevel.INFO;

    private @Nullable Supplier<? extends KeelEventLog> baseLogBuilder;

    public KeelEventLoggerImpl(
            @Nonnull String presetTopic,
            @Nonnull Supplier<KeelEventLogCenter> eventLogCenterSupplier
    ) {
        this(presetTopic, eventLogCenterSupplier, null);
    }

    public KeelEventLoggerImpl(
            @Nonnull String presetTopic,
            @Nonnull Supplier<KeelEventLogCenter> eventLogCenterSupplier,
            @Nullable Supplier<? extends KeelEventLog> baseLogBuilder
    ) {
        this.presetTopic = presetTopic;
        this.eventLogCenterSupplier = eventLogCenterSupplier;
        this.baseLogBuilder = baseLogBuilder;
    }

    @Nonnull
    @Override
    public KeelLogLevel getVisibleLevel() {
        return visibleLogLevel;
    }

    @Override
    public void setVisibleLevel(@Nonnull KeelLogLevel level) {
        this.visibleLogLevel = level;
    }

    @Nonnull
    @Override
    public Supplier<KeelEventLogCenter> getEventLogCenterSupplier() {
        return eventLogCenterSupplier;
    }

    @Nonnull
    @Override
    public String getPresetTopic() {
        return presetTopic;
    }

    @Nonnull
    @Override
    public Supplier<? extends KeelEventLog> getBaseLogBuilder() {
        return Objects.requireNonNullElse(baseLogBuilder, (Supplier<KeelEventLogImpl>) () -> new KeelEventLogImpl(KeelLogLevel.INFO, getPresetTopic()));
    }

    @Override
    public void setBaseLogBuilder(@Nullable Supplier<? extends KeelEventLog> baseLogBuilder) {
        this.baseLogBuilder = baseLogBuilder;
    }

    /**
     * @since 3.0.10
     */
    private final List<KeelEventLogger> bypassLoggerList = new ArrayList<>();

    /**
     * @since 3.0.10
     */
    @Override
    public void addBypassLogger(@Nonnull KeelEventLogger bypassLogger) {
        this.bypassLoggerList.add(bypassLogger);
    }

    /**
     * @since 3.0.10
     */
    @Nonnull
    @Override
    public List<KeelEventLogger> getBypassLoggers() {
        return this.bypassLoggerList;
    }
}
