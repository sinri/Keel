package io.github.sinri.keel.test.queue;

import io.github.sinri.keel.Keel;
import io.github.sinri.keel.core.logger.KeelLogger;
import io.github.sinri.keel.queue.KeelSyncQueueTask;
import io.vertx.core.json.JsonObject;

public class QueueTask extends KeelSyncQueueTask {

    private final int taskID;
    private final double value;

    public QueueTask(int taskID, double value) {
        this.taskID = taskID;
        this.value = value;
    }

    protected KeelLogger getLogger() {
        return Keel.outputLogger("QueueTask");
    }

    @Override
    public String getTaskReference() {
        return "QueueTask#" + taskID;
    }

    @Override
    public boolean declareTaskToRun() {
        return !(value > 0.9);
    }

    @Override
    public String runTask() throws Exception {
        if (value > 0.6) {
            throw new Exception("value is over 0.6: " + value);
        }
        return "value is " + value;
    }

    @Override
    public void declareTaskFinished(boolean isDone, String feedback) {
        getLogger().info("declareTaskFinished", new JsonObject().put("is_done", isDone).put("feedback", feedback));
    }
}
