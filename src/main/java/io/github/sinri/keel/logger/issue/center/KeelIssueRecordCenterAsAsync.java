package io.github.sinri.keel.logger.issue.center;

import io.github.sinri.keel.logger.issue.core.KeelIssueRecordCenter;
import io.github.sinri.keel.logger.issue.core.KeelIssueRecorderAdapterAsync;

import javax.annotation.Nonnull;

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
