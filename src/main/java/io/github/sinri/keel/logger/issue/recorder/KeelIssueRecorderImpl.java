package io.github.sinri.keel.logger.issue.recorder;

import io.github.sinri.keel.core.TechnicalPreview;
import io.github.sinri.keel.logger.issue.center.KeelIssueRecordCenter;
import io.github.sinri.keel.logger.issue.record.KeelIssueRecord;

import javax.annotation.Nonnull;
import java.util.function.Supplier;

/**
 * @since 3.1.10
 */
@TechnicalPreview(since = "3.1.10")
public class KeelIssueRecorderImpl<T extends KeelIssueRecord<?>, R> implements KeelIssueRecorder<T, R> {
    protected final @Nonnull Supplier<T> issueRecordBuilder;
    private final @Nonnull KeelIssueRecordCenter<R> issueRecordCenter;
    private final @Nonnull String topic;

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
