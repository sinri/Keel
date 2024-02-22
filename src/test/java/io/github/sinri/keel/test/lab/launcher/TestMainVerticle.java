package io.github.sinri.keel.test.lab.launcher;

import io.github.sinri.keel.facade.async.KeelAsyncKit;
import io.github.sinri.keel.logger.event.KeelEventLog;
import io.github.sinri.keel.logger.issue.center.KeelIssueRecordCenter;
import io.github.sinri.keel.verticles.KeelVerticleBase;
import io.vertx.core.Future;

public class TestMainVerticle extends KeelVerticleBase<KeelEventLog> {


    @Override
    public void start() throws Exception {
        setRoutineIssueRecorder(KeelIssueRecordCenter.outputCenter().generateRecorder(getClass().getName(), () -> new KeelEventLog(getClass().getName())));

        KeelAsyncKit.endless(() -> {
            getRoutineIssueRecorder().info(r -> r.message("X"));
            return Future.succeededFuture();
        });
    }
}
