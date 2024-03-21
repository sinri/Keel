package io.github.sinri.keel.logger.issue.recorder;

import io.github.sinri.keel.core.TechnicalPreview;
import io.github.sinri.keel.logger.KeelLogLevel;
import io.github.sinri.keel.logger.issue.center.KeelIssueRecordCenter;
import io.github.sinri.keel.logger.issue.record.KeelIssueRecord;
import io.vertx.core.Handler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * @since 3.1.10
 */
@TechnicalPreview(since = "3.1.10")
class KeelIssueRecorderImpl<T extends KeelIssueRecord<?>> implements KeelIssueRecorder<T> {
    protected final @Nonnull Supplier<T> issueRecordBuilder;
    private final @Nonnull KeelIssueRecordCenter issueRecordCenter;
    private final @Nonnull String topic;
    private KeelLogLevel visibleLevel = KeelLogLevel.INFO;

    public KeelIssueRecorderImpl(
            @Nonnull KeelIssueRecordCenter issueRecordCenter,
            @Nonnull Supplier<T> issueRecordBuilder,
            @Nonnull String topic
    ) {
        this.issueRecordCenter = issueRecordCenter;
        this.issueRecordBuilder = issueRecordBuilder;
        this.topic = topic;
    }

    @Nonnull
    @Override
    public KeelLogLevel getVisibleLevel() {
        return visibleLevel;
    }

    @Override
    public void setVisibleLevel(@Nonnull KeelLogLevel visibleLevel) {
        this.visibleLevel = visibleLevel;
    }

    @Nonnull
    @Override
    public KeelIssueRecordCenter issueRecordCenter() {
        return issueRecordCenter;
    }

    /**
     * @return an instance of issue, to be modified for details.
     */
    @Nonnull
    @Override
    public Supplier<T> issueRecordBuilder() {
        return issueRecordBuilder;
    }

    private final List<KeelIssueRecorder<T>> bypassIssueRecorders = new ArrayList<>();
    @Nullable
    Handler<T> recordFormatter = null;

    /**
     * @since 3.2.0
     */
    @Override
    public void addBypassIssueRecorder(@Nonnull KeelIssueRecorder<T> bypassIssueRecorder) {
        bypassIssueRecorders.add(bypassIssueRecorder);
    }

    @Nonnull
    @Override
    public String topic() {
        return topic;
    }

    /**
     * @since 3.2.0
     */
    @Nonnull
    @Override
    public List<KeelIssueRecorder<T>> getBypassIssueRecorders() {
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
