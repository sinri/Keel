package io.github.sinri.keel.logger.issue.center;

import io.github.sinri.keel.logger.issue.core.KeelIssueRecordCenter;
import io.github.sinri.keel.logger.issue.core.KeelIssueRecorderAdapter;

import javax.annotation.Nonnull;

public class KeelIssueRecordCenterAsSync<R> implements KeelIssueRecordCenter<R> {
    protected final @Nonnull KeelIssueRecorderAdapter<R> adapter;

    public KeelIssueRecordCenterAsSync(@Nonnull KeelIssueRecorderAdapter<R> adapter) {
        this.adapter = adapter;
    }

    @Nonnull
    @Override
    public KeelIssueRecorderAdapter<R> getAdapter() {
        return adapter;
    }
}
