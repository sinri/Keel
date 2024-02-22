package io.github.sinri.keel.servant.queue;

import io.github.sinri.keel.logger.issue.record.event.RoutineBaseIssueRecord;

import javax.annotation.Nonnull;

/**
 * @since 3.2.0
 */
public final class QueueTaskIssueRecord extends RoutineBaseIssueRecord<QueueTaskIssueRecord> {
    public static final String TopicQueue = "Queue";

    public QueueTaskIssueRecord(@Nonnull String taskReference, @Nonnull String taskCategory) {
        super(TopicQueue);
        this.classification("reference:" + taskReference, "category:" + taskCategory);
    }

    @Nonnull
    @Override
    public QueueTaskIssueRecord getImplementation() {
        return this;
    }

}
