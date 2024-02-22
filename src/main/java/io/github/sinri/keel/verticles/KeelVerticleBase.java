package io.github.sinri.keel.verticles;

import io.github.sinri.keel.logger.issue.center.KeelIssueRecordCenter;
import io.github.sinri.keel.logger.issue.record.KeelIssueRecord;
import io.github.sinri.keel.logger.issue.recorder.KeelIssueRecorder;
import io.vertx.core.AbstractVerticle;

import javax.annotation.Nonnull;

abstract public class KeelVerticleBase<T extends KeelIssueRecord<T>> extends AbstractVerticle implements KeelVerticle<T> {
    private @Nonnull KeelIssueRecorder<T> routineIssueRecorder;

    public KeelVerticleBase() {
        this.routineIssueRecorder = KeelIssueRecordCenter.createSilentIssueRecorder();
    }

    @Nonnull
    @Override
    public KeelIssueRecorder<T> getRoutineIssueRecorder() {
        return routineIssueRecorder;
    }

    @Override
    public void setRoutineIssueRecorder(@Nonnull KeelIssueRecorder<T> routineIssueRecorder) {
        this.routineIssueRecorder = routineIssueRecorder;
    }
}
