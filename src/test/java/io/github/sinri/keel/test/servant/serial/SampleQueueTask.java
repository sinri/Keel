package io.github.sinri.keel.test.servant.serial;

import io.github.sinri.keel.servant.KeelServantQueueTask;
import io.vertx.core.Future;

public class SampleQueueTask extends KeelServantQueueTask {

    int taskId;

    public SampleQueueTask(int taskId) {
        this.taskId = taskId;
    }

    @Override
    public String getTaskReference() {
        return "TASK_" + taskId;
    }

    @Override
    public Future<String> execute() {
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (taskId % 2 == 0) {
            return Future.succeededFuture(getTaskReference() + " DONE");
        }
        return Future.failedFuture(getTaskReference() + " FAILED");
    }
}
