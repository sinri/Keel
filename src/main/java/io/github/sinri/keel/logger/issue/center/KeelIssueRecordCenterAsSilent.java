package io.github.sinri.keel.logger.issue.center;

import io.github.sinri.keel.logger.issue.core.KeelIssueRecordCenter;
import io.github.sinri.keel.logger.issue.core.KeelIssueRecorderAdapter;
import io.github.sinri.keel.logger.issue.recorder.adapter.SilentAdapter;

import javax.annotation.Nonnull;

public class KeelIssueRecordCenterAsSilent implements KeelIssueRecordCenter<String> {
    private static final KeelIssueRecordCenterAsSilent instance = new KeelIssueRecordCenterAsSilent();

    private KeelIssueRecordCenterAsSilent() {

    }

    public static KeelIssueRecordCenterAsSilent getInstance() {
        return instance;
    }

    @Nonnull
    @Override
    public KeelIssueRecorderAdapter<String> getAdapter() {
        return SilentAdapter.getInstance();
    }
}
