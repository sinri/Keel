package io.github.sinri.keel.logger.issue.center;

import io.github.sinri.keel.core.TechnicalPreview;
import io.github.sinri.keel.logger.issue.recorder.adapter.KeelIssueRecorderAdapterAsync;

import javax.annotation.Nonnull;

/**
 * @since 3.1.10
 */
@TechnicalPreview(since = "3.1.10")
public class KeelIssueRecordCenterAsAsync<R> implements KeelIssueRecordCenter<R> {
    protected final @Nonnull KeelIssueRecorderAdapterAsync<R> adapter;

    public KeelIssueRecordCenterAsAsync(@Nonnull KeelIssueRecorderAdapterAsync<R> adapter) {
        this.adapter = adapter;
    }

    @Nonnull
    @Override
    public KeelIssueRecorderAdapterAsync<R> getAdapter() {
        return adapter;
    }
}
