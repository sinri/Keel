package io.github.sinri.keel.verticles;

import io.github.sinri.keel.logger.issue.record.KeelIssueRecord;
import io.github.sinri.keel.logger.issue.recorder.KeelIssueRecorder;

import javax.annotation.Nonnull;

abstract public class KeelVerticleImplWithIssueRecorder<T extends KeelIssueRecord<T>> extends KeelVerticleImplPure {
    private final @Nonnull KeelIssueRecorder<T> issueRecorder;

    public KeelVerticleImplWithIssueRecorder() {
        this.issueRecorder = buildIssueRecorder();
    }

    @Nonnull
    public KeelIssueRecorder<T> getIssueRecorder() {
        return issueRecorder;
    }

    abstract protected @Nonnull KeelIssueRecorder<T> buildIssueRecorder();
}
