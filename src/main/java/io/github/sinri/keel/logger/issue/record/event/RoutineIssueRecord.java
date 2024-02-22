package io.github.sinri.keel.logger.issue.record.event;

import javax.annotation.Nonnull;

/**
 * @since 3.2.0
 */
public final class RoutineIssueRecord extends RoutineBaseIssueRecord<RoutineIssueRecord> {

    public RoutineIssueRecord(@Nonnull String topic) {
        super(topic);
    }

    @Nonnull
    @Override
    public RoutineIssueRecord getImplementation() {
        return this;
    }
}
