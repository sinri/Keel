package io.github.sinri.keel.servant.queue;

import io.github.sinri.keel.logger.issue.record.event.RoutineBaseIssueRecord;

import javax.annotation.Nonnull;

public final class QueueManageIssueRecord extends RoutineBaseIssueRecord<QueueManageIssueRecord> {
    public static final String TopicQueue = "Queue";

    public QueueManageIssueRecord() {
        super(TopicQueue);
    }

    @Nonnull
    @Override
    public QueueManageIssueRecord getImplementation() {
        return this;
    }
}
