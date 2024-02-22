package io.github.sinri.keel.logger.issue.record.event;

import io.github.sinri.keel.logger.issue.record.BaseIssueRecord;

import javax.annotation.Nonnull;

/**
 * @since 3.2.0
 */
public final class RoutineIssueRecord extends BaseIssueRecord<RoutineIssueRecord> {
    private final @Nonnull String topic;

    public RoutineIssueRecord(@Nonnull String topic) {
        this.topic = topic;
    }

    @Nonnull
    @Override
    public RoutineIssueRecord getImplementation() {
        return this;
    }

    @Nonnull
    @Override
    public String topic() {
        return topic;
    }

}

