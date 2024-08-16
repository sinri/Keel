package io.github.sinri.keel.servant.queue;

import io.github.sinri.keel.logger.issue.record.BaseIssueRecord;
import org.jetbrains.annotations.NotNull;



/**
 * @since 3.2.0
 */
public final class QueueTaskIssueRecord extends BaseIssueRecord<QueueTaskIssueRecord> {
    public static final String TopicQueue = "Queue";

    public QueueTaskIssueRecord(@NotNull String taskReference, @NotNull String taskCategory) {
        super();
        this.classification("task", "reference:" + taskReference, "category:" + taskCategory);
    }


    @Override
    public @NotNull String topic() {
        return TopicQueue;
    }


    @Override
    public @NotNull QueueTaskIssueRecord getImplementation() {
        return this;
    }
}
