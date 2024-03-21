package io.github.sinri.keel.servant.queue;

import io.github.sinri.keel.logger.issue.record.BaseIssueRecord;

import javax.annotation.Nonnull;

public final class QueueManageIssueRecord extends BaseIssueRecord<QueueManageIssueRecord> {
    public static final String TopicQueue = "Queue";

    public QueueManageIssueRecord() {
        super();
        this.classification("manage");
    }


    @Nonnull
    @Override
    public QueueManageIssueRecord getImplementation() {
        return this;
    }

    @Nonnull
    @Override
    public String topic() {
        return TopicQueue;
    }
}
