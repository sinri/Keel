package io.github.sinri.keel.logger.issue.center;

import io.github.sinri.keel.core.TechnicalPreview;
import io.github.sinri.keel.logger.issue.record.KeelIssueRecord;
import io.github.sinri.keel.logger.issue.recorder.KeelIssueRecorder;
import io.github.sinri.keel.logger.issue.recorder.adapter.KeelIssueRecorderAdapter;
import io.github.sinri.keel.logger.issue.recorder.adapter.SyncStdoutAdapter;

import javax.annotation.Nonnull;
import java.util.function.Supplier;

/**
 * @since 3.1.10 Technical Preview
 */
@TechnicalPreview(since = "3.1.10")
public interface KeelIssueRecordCenter {
    static KeelIssueRecordCenter OutputCenter() {
        return new KeelIssueRecordCenterAsSync(SyncStdoutAdapter.getInstance());
    }

    static KeelIssueRecordCenter SilentCenter() {
        return KeelIssueRecordCenterAsSilent.getInstance();
    }

    @Nonnull
    KeelIssueRecorderAdapter getAdapter();

    /**
     * @param issueRecordBuilder Sample for silent: {@code Supplier<T> issueRecordBuilder= () -> null;}
     */
    @Nonnull
    default <T extends KeelIssueRecord<?>> KeelIssueRecorder<T> generateRecorder(@Nonnull String topic, @Nonnull Supplier<T> issueRecordBuilder) {
        return KeelIssueRecorder.build(this, issueRecordBuilder, topic);
    }
}
