package io.github.sinri.keel.logger.issue.center;

import io.github.sinri.keel.core.TechnicalPreview;
import io.github.sinri.keel.logger.issue.record.KeelIssueRecord;
import io.github.sinri.keel.logger.issue.recorder.KeelIssueRecorder;
import io.github.sinri.keel.logger.issue.recorder.adapter.KeelIssueRecorderAdapter;
import io.github.sinri.keel.logger.issue.recorder.adapter.SyncStdoutAdapter;

import javax.annotation.Nonnull;
import java.util.function.Supplier;

/**
 * @param <R> The type that the issue record would be rendered to.
 * @since 3.1.10 Technical Preview
 */
@TechnicalPreview(since = "3.1.10")
public interface KeelIssueRecordCenter<R> {
    @Nonnull
    KeelIssueRecorderAdapter<R> getAdapter();

    static KeelIssueRecordCenter<String> OutputCenter() {
        return new KeelIssueRecordCenterAsSync<>(SyncStdoutAdapter.getInstance());
    }

    static KeelIssueRecordCenter<String> SilentCenter() {
        return KeelIssueRecordCenterAsSilent.getInstance();
    }

    /**
     * @param issueRecordBuilder Sample for silent: {@code Supplier<T> issueRecordBuilder= () -> null;}
     */
    @Nonnull
    default <T extends KeelIssueRecord<?>> KeelIssueRecorder<T, R> generateRecorder(@Nonnull String topic, @Nonnull Supplier<T> issueRecordBuilder) {
        return KeelIssueRecorder.build(this, issueRecordBuilder, topic);
    }
}
