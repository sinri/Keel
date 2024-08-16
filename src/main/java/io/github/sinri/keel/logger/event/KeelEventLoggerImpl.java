package io.github.sinri.keel.logger.event;

import io.github.sinri.keel.logger.KeelLogLevel;
import io.github.sinri.keel.logger.issue.recorder.KeelIssueRecorder;
import io.vertx.core.Handler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * @since 3.2.0
 */
class KeelEventLoggerImpl implements KeelEventLogger {
    private final @NotNull KeelIssueRecorder<KeelEventLog> issueRecorder;
    private final @Nullable Handler<KeelEventLog> templateEventLogEditor;
    private final @NotNull List<KeelEventLogger> bypassLoggers;
    private @Nullable Handler<KeelEventLog> dynamicFormatter;

    KeelEventLoggerImpl(@NotNull KeelIssueRecorder<KeelEventLog> issueRecorder, @Nullable Handler<KeelEventLog> templateEventLogEditor) {
        this.issueRecorder = issueRecorder;
        this.templateEventLogEditor = templateEventLogEditor;
        this.bypassLoggers = new ArrayList<>();
    }

    @Nullable
    @Override
    public Handler<KeelEventLog> templateEventLogEditor() {
        return templateEventLogEditor;
    }

    /**
     * @since 3.2.7
     */
    @Override
    public void setDynamicEventLogFormatter(@Nullable Handler<KeelEventLog> formatter) {
        this.dynamicFormatter = formatter;
    }

    @NotNull
    private KeelIssueRecorder<KeelEventLog> getIssueRecorder() {
        return issueRecorder;
    }

    @Override
    public void addBypassLogger(@NotNull KeelEventLogger bypassLogger) {
        this.bypassLoggers.add(bypassLogger);
    }


    @Override
    public @NotNull List<KeelEventLogger> getBypassLoggers() {
        return bypassLoggers;
    }


    @Override
    public @NotNull KeelLogLevel getVisibleLevel() {
        return this.getIssueRecorder().getVisibleLevel();
    }

    @Override
    public void setVisibleLevel(@NotNull KeelLogLevel level) {
        this.getIssueRecorder().setVisibleLevel(level);
    }


    @Override
    public @NotNull String getPresetTopic() {
        return this.getIssueRecorder().topic();
    }

    @Override
    public void log(@NotNull Handler<KeelEventLog> eventLogHandler) {
        Handler<KeelEventLog> h = r -> {
            var x = templateEventLogEditor();
            if (x != null) {
                x.handle(r);
            }
            eventLogHandler.handle(r);
            // since 3.2.7
            if (dynamicFormatter != null) {
                dynamicFormatter.handle(r);
            }
        };

        this.getIssueRecorder().record(h);

        getBypassLoggers().forEach(bypassLogger -> {
            bypassLogger.log(h);
        });
    }
}
