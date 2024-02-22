package io.github.sinri.keel.test.lab.queue;

import io.github.sinri.keel.facade.async.KeelAsyncKit;
import io.github.sinri.keel.logger.issue.center.KeelIssueRecordCenter;
import io.github.sinri.keel.logger.issue.recorder.KeelIssueRecorder;
import io.github.sinri.keel.servant.queue.KeelQueueTask;
import io.github.sinri.keel.servant.queue.QueueTaskIssueRecord;
import io.vertx.core.Future;

public class TestQueueTask extends KeelQueueTask {
    String id;
    int life;

    public TestQueueTask(String id, int life) {
        this.id = id;
        this.life = life;
    }

    @Override
    public String getTaskReference() {
        return id;
    }

    @Override
    public String getTaskCategory() {
        return "TEST";
    }

    /**
     * @since 3.2.0
     */
    @Override
    protected KeelIssueRecorder<QueueTaskIssueRecord> prepareRoutineIssueRecord() {
        KeelIssueRecorder<QueueTaskIssueRecord> x = KeelIssueRecordCenter.outputCenter().generateRecorder("TestQueue", () -> new QueueTaskIssueRecord(getTaskReference(), getTaskCategory()));
        x.setRecordFormatter(r -> r.context("id", id).context("life", life));
        return x;
    }

    @Override
    protected Future<Void> run() {
        getRoutineIssueRecorder().info(r -> r.message("START"));
        return KeelAsyncKit.sleep(this.life * 1000L)
                .eventually(() -> {
                    getRoutineIssueRecorder().info(r -> r.message("END"));
                    return Future.succeededFuture();
                });
    }
}
