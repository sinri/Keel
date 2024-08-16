package io.github.sinri.keel.logger.event;

import io.github.sinri.keel.logger.issue.record.BaseIssueRecord;
import org.jetbrains.annotations.NotNull;


/**
 * @since 3.2.0
 */
public final class KeelEventLog extends BaseIssueRecord<KeelEventLog> {
    private @NotNull String topic;

    public KeelEventLog(@NotNull String topic) {
        this.topic = topic;
    }

    @NotNull
    @Override
    public KeelEventLog getImplementation() {
        return this;
    }

    @Override
    public @NotNull String topic() {
        return topic;
    }

    @NotNull
    public KeelEventLog topic(@NotNull String topic) {
        this.topic = topic;
        return this;
    }

}

