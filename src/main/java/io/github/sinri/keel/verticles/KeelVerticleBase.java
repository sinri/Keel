package io.github.sinri.keel.verticles;

import io.github.sinri.keel.logger.issue.center.KeelIssueRecordCenter;
import io.github.sinri.keel.logger.issue.record.event.RoutineBaseIssueRecord;
import io.github.sinri.keel.logger.issue.recorder.KeelIssueRecorder;
import io.vertx.core.AbstractVerticle;

import javax.annotation.Nonnull;

abstract public class KeelVerticleBase<T extends RoutineBaseIssueRecord<?>> extends AbstractVerticle implements KeelVerticle<T> {
    private @Nonnull KeelIssueRecorder<RoutineBaseIssueRecord<T>> routineIssueRecorder;

    public KeelVerticleBase() {
        this.routineIssueRecorder = KeelIssueRecordCenter.createSilentIssueRecorder();
    }

    @Nonnull
    @Override
    public KeelIssueRecorder<RoutineBaseIssueRecord<T>> getRoutineIssueRecorder() {
        return routineIssueRecorder;
    }

    @Override
    public void setRoutineIssueRecorder(@Nonnull KeelIssueRecorder<RoutineBaseIssueRecord<T>> routineIssueRecorder) {
        this.routineIssueRecorder = routineIssueRecorder;
    }
}
