package io.github.sinri.keel.test.lab.queue;

import io.github.sinri.keel.facade.Keel;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.VertxOptions;

public class TestQueueMain {
    public static void main(String[] args) {
        Keel.initializeVertxStandalone(new VertxOptions());

        TestQueue testQueue = new TestQueue();
        testQueue.deployMe(new DeploymentOptions().setWorker(true));
    }
}
