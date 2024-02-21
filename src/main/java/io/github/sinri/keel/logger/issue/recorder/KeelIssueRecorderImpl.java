package io.github.sinri.keel.logger.issue.recorder;

import io.github.sinri.keel.core.TechnicalPreview;
import io.github.sinri.keel.logger.KeelLogLevel;
import io.github.sinri.keel.logger.issue.center.KeelIssueRecordCenter;
import io.github.sinri.keel.logger.issue.record.KeelIssueRecord;

import javax.annotation.Nonnull;
import java.util.function.Supplier;

/**
 * @since 3.1.10
 */
@TechnicalPreview(since = "3.1.10")
class KeelIssueRecorderImpl<T extends KeelIssueRecord<?>, R> implements KeelIssueRecorder<T, R> {
    protected final @Nonnull Supplier<T> issueRecordBuilder;
    private final @Nonnull KeelIssueRecordCenter<R> issueRecordCenter;
    private final @Nonnull String topic;
    private KeelLogLevel visibleLevel = KeelLogLevel.INFO;

    public KeelIssueRecorderImpl(
            @Nonnull KeelIssueRecordCenter<R> issueRecordCenter,
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
    public KeelIssueRecordCenter<R> issueRecordCenter() {
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

    @Nonnull
    @Override
    public String topic() {
        return topic;
    }
}
