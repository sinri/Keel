package io.github.sinri.keel.logger.event;

import io.github.sinri.keel.logger.issue.record.BaseIssueRecord;

import javax.annotation.Nonnull;

/**
 * @since 3.2.0
 */
public final class KeelEventLog extends BaseIssueRecord<KeelEventLog> {
    private @Nonnull String topic;

    public KeelEventLog(@Nonnull String topic) {
        this.topic = topic;
    }

    @Nonnull
    @Override
    public KeelEventLog getImplementation() {
        return this;
    }

    @Nonnull
    @Override
    public String topic() {
        return topic;
    }

    @Nonnull
    public KeelEventLog topic(@Nonnull String topic) {
        this.topic = topic;
        return this;
    }

}

