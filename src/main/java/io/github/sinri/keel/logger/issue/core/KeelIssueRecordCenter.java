package io.github.sinri.keel.logger.issue.core;

import io.github.sinri.keel.core.TechnicalPreview;
import io.github.sinri.keel.logger.issue.recorder.KeelIssueRecorderImpl;

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

    /**
     * @param issueRecordBuilder Sample for silent: {@code Supplier<T> issueRecordBuilder= () -> null;}
     */
    @Nonnull
    default <T extends KeelIssueRecord> KeelIssueRecorder<T, R> generateRecorder(@Nonnull String topic, @Nonnull Supplier<T> issueRecordBuilder) {
        return new KeelIssueRecorderImpl<>(this, issueRecordBuilder, topic);
    }
}
