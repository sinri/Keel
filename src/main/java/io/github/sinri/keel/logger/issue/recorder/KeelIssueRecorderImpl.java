package io.github.sinri.keel.logger.issue.recorder;

import io.github.sinri.keel.core.TechnicalPreview;
import io.github.sinri.keel.logger.KeelLogLevel;
import io.github.sinri.keel.logger.issue.center.KeelIssueRecordCenter;
import io.github.sinri.keel.logger.issue.record.KeelIssueRecord;
import io.vertx.core.Handler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * @since 3.1.10
 */
@TechnicalPreview(since = "3.1.10")
class KeelIssueRecorderImpl<T extends KeelIssueRecord<?>> implements KeelIssueRecorder<T> {
    protected final @NotNull Supplier<T> issueRecordBuilder;
    private final @NotNull KeelIssueRecordCenter issueRecordCenter;
    private final @NotNull String topic;
    private KeelLogLevel visibleLevel = KeelLogLevel.INFO;

    public KeelIssueRecorderImpl(
            @NotNull KeelIssueRecordCenter issueRecordCenter,
            @NotNull Supplier<T> issueRecordBuilder,
            @NotNull String topic
    ) {
        this.issueRecordCenter = issueRecordCenter;
        this.issueRecordBuilder = issueRecordBuilder;
        this.topic = topic;
    }


    @Override
    public @NotNull KeelLogLevel getVisibleLevel() {
        return visibleLevel;
    }

    @Override
    public void setVisibleLevel(@NotNull KeelLogLevel visibleLevel) {
        this.visibleLevel = visibleLevel;
    }


    @Override
    public @NotNull KeelIssueRecordCenter issueRecordCenter() {
        return issueRecordCenter;
    }

    /**
     * @return an instance of issue, to be modified for details.
     */

    @Override
    public @NotNull Supplier<T> issueRecordBuilder() {
        return issueRecordBuilder;
    }

    private final List<KeelIssueRecorder<T>> bypassIssueRecorders = new ArrayList<>();
    @Nullable
    Handler<T> recordFormatter = null;

    /**
     * @since 3.2.0
     */
    @Override
    public void addBypassIssueRecorder(@NotNull KeelIssueRecorder<T> bypassIssueRecorder) {
        bypassIssueRecorders.add(bypassIssueRecorder);
    }

    @Override
    public @NotNull String topic() {
        return topic;
    }

    /**
     * @since 3.2.0
     */
    @Override
    public @NotNull List<KeelIssueRecorder<T>> getBypassIssueRecorders() {
        return bypassIssueRecorders;
    }

    @Nullable
    @Override
    public Handler<T> getRecordFormatter() {
        return recordFormatter;
    }

    @Override
    public void setRecordFormatter(@Nullable Handler<T> handler) {
        this.recordFormatter = handler;
    }
}
