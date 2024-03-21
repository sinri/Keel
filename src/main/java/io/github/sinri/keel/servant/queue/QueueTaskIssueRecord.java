package io.github.sinri.keel.servant.queue;

import io.github.sinri.keel.logger.issue.record.BaseIssueRecord;

import javax.annotation.Nonnull;

/**
 * @since 3.2.0
 */
public final class QueueTaskIssueRecord extends BaseIssueRecord<QueueTaskIssueRecord> {
    public static final String TopicQueue = "Queue";

    public QueueTaskIssueRecord(@Nonnull String taskReference, @Nonnull String taskCategory) {
        this.classification("task", "reference:" + taskReference, "category:" + taskCategory);
    }

    @Nonnull
    @Override
    public String topic() {
        return TopicQueue;
    }

    @Nonnull
    @Override
    public QueueTaskIssueRecord getImplementation() {
        return this;
    }

}
