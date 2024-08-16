package io.github.sinri.keel.test.lab.queue;

import io.github.sinri.keel.logger.issue.center.KeelIssueRecordCenter;
import io.github.sinri.keel.logger.issue.recorder.KeelIssueRecorder;
import io.github.sinri.keel.servant.queue.KeelQueue;
import io.github.sinri.keel.servant.queue.KeelQueueNextTaskSeeker;
import io.github.sinri.keel.servant.queue.QueueManageIssueRecord;
import io.github.sinri.keel.servant.queue.QueueWorkerPoolManager;
import io.vertx.core.Future;
import org.jetbrains.annotations.NotNull;



public class TestQueue extends KeelQueue {

    @Override
    protected @NotNull KeelQueueNextTaskSeeker getNextTaskSeeker() {
        return new TestQueueTaskSeeker();
    }


    @Override
    protected @NotNull SignalReader getSignalReader() {
        return new SignalReader() {
            @Override
            public Future<QueueSignal> readSignal() {
                return Future.succeededFuture(QueueSignal.RUN);
            }
        };
    }


    @Override
    protected @NotNull QueueWorkerPoolManager getQueueWorkerPoolManager() {
        return new QueueWorkerPoolManager(3);
    }


    @Override
    protected @NotNull KeelIssueRecorder<QueueManageIssueRecord> buildIssueRecorder() {
        return KeelIssueRecordCenter.outputCenter().generateIssueRecorder(QueueManageIssueRecord.TopicQueue, QueueManageIssueRecord::new);
    }
}
