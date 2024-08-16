package io.github.sinri.keel.servant.queue;

import io.github.sinri.keel.logger.issue.record.BaseIssueRecord;
import org.jetbrains.annotations.NotNull;



public final class QueueManageIssueRecord extends BaseIssueRecord<QueueManageIssueRecord> {
    public static final String TopicQueue = "Queue";

    public QueueManageIssueRecord() {
        super();
        this.classification("manage");
    }


    @Override
    public @NotNull QueueManageIssueRecord getImplementation() {
        return this;
    }

    @Override
    public @NotNull String topic() {
        return TopicQueue;
    }
}
