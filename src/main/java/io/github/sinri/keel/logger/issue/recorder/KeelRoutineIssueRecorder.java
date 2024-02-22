package io.github.sinri.keel.logger.issue.recorder;

import io.github.sinri.keel.logger.issue.center.KeelIssueRecordCenter;
import io.github.sinri.keel.logger.issue.record.event.RoutineBaseIssueRecord;
import io.github.sinri.keel.logger.issue.record.event.RoutineIssueRecord;

import javax.annotation.Nonnull;

/**
 * @since 3.2.0
 * To be a replacement of KeelEventLogger.
 * AS {@code KeelIssueRecorder<RoutineIssueRecord>}.
 */
class KeelRoutineIssueRecorder extends KeelIssueRecorderImpl<RoutineBaseIssueRecord<RoutineIssueRecord>> {
    public KeelRoutineIssueRecorder(
            @Nonnull KeelIssueRecordCenter issueRecordCenter,
            @Nonnull String topic
    ) {
        super(issueRecordCenter, () -> new RoutineIssueRecord(topic), topic);
    }
}
