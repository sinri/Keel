package io.github.sinri.keel.verticles;

import io.github.sinri.keel.logger.issue.center.KeelIssueRecordCenter;
import io.github.sinri.keel.logger.issue.record.KeelIssueRecord;
import io.github.sinri.keel.logger.issue.recorder.KeelIssueRecorder;
import io.vertx.core.AbstractVerticle;

import javax.annotation.Nonnull;

abstract public class KeelVerticleBase<T extends KeelIssueRecord<T>> extends AbstractVerticle implements KeelVerticle<T> {
    private @Nonnull KeelIssueRecorder<T> issueRecorder;

    public KeelVerticleBase() {
        this.issueRecorder = KeelIssueRecordCenter.createSilentIssueRecorder();
    }

    @Nonnull
    @Override
    public KeelIssueRecorder<T> getIssueRecorder() {
        return issueRecorder;
    }

    @Override
    public void setIssueRecorder(@Nonnull KeelIssueRecorder<T> issueRecorder) {
        this.issueRecorder = issueRecorder;
    }
}
