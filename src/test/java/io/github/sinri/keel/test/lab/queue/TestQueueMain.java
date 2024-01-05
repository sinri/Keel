package io.github.sinri.keel.test.lab.queue;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.ThreadingModel;
import io.vertx.core.VertxOptions;

import static io.github.sinri.keel.facade.KeelInstance.keel;

public class TestQueueMain {
    public static void main(String[] args) {
        keel.initializeVertxStandalone(new VertxOptions());

        TestQueue testQueue = new TestQueue();
        testQueue.deployMe(new DeploymentOptions().setThreadingModel(ThreadingModel.WORKER));
    }
}
