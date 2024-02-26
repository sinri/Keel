package io.github.sinri.keel.logger.event;

import io.github.sinri.keel.logger.KeelLogLevel;
import io.github.sinri.keel.logger.issue.recorder.KeelIssueRecorder;
import io.vertx.core.Handler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * @since 3.2.0
 */
class KeelEventLoggerImpl implements KeelEventLogger {
    private final @Nonnull KeelIssueRecorder<KeelEventLog> issueRecorder;
    private final @Nullable Handler<KeelEventLog> templateEventLogEditor;
    private final @Nonnull List<KeelEventLogger> bypassLoggers;

    KeelEventLoggerImpl(@Nonnull KeelIssueRecorder<KeelEventLog> issueRecorder, @Nullable Handler<KeelEventLog> templateEventLogEditor) {
        this.issueRecorder = issueRecorder;
        this.templateEventLogEditor = templateEventLogEditor;
        this.bypassLoggers = new ArrayList<>();
    }

    @Nullable
    @Override
    public Handler<KeelEventLog> templateEventLogEditor() {
        return templateEventLogEditor;
    }

    @Nonnull
    private KeelIssueRecorder<KeelEventLog> getIssueRecorder() {
        return issueRecorder;
    }

    @Override
    public void addBypassLogger(@Nonnull KeelEventLogger bypassLogger) {
        this.bypassLoggers.add(bypassLogger);
    }

    @Nonnull
    @Override
    public List<KeelEventLogger> getBypassLoggers() {
        return bypassLoggers;
    }

    @Nonnull
    @Override
    public KeelLogLevel getVisibleLevel() {
        return this.getIssueRecorder().getVisibleLevel();
    }

    @Override
    public void setVisibleLevel(@Nonnull KeelLogLevel level) {
        this.getIssueRecorder().setVisibleLevel(level);
    }

    @Nonnull
    @Override
    public String getPresetTopic() {
        return this.getIssueRecorder().topic();
    }

    @Override
    public void log(@Nonnull Handler<KeelEventLog> eventLogHandler) {
        this.getIssueRecorder().record(r -> {
            var x = templateEventLogEditor();
            if (x != null) {
                x.handle(r);
            }
            eventLogHandler.handle(r);
        });

        getBypassLoggers().forEach(bypassLogger -> {
            bypassLogger.log(r -> {
                var x = templateEventLogEditor();
                if (x != null) {
                    x.handle(r);
                }
                eventLogHandler.handle(r);
            });
        });
    }
}
