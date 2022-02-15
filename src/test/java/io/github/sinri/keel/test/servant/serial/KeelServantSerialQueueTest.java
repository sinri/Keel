package io.github.sinri.keel.test.servant.serial;

import io.github.sinri.keel.Keel;
import io.github.sinri.keel.core.logger.KeelLogLevel;
import io.github.sinri.keel.core.logger.KeelLogger;
import io.github.sinri.keel.servant.KeelServantQueueTask;
import io.github.sinri.keel.servant.KeelServantSerialQueue;
import io.github.sinri.keel.test.servant.SampleQueueTask;
import io.vertx.core.Future;
import io.vertx.core.VertxOptions;

import java.util.Random;

public class KeelServantSerialQueueTest extends KeelServantSerialQueue {
    static Random random = new Random();

    public KeelServantSerialQueueTest(long sleepPeriod) {
        super(sleepPeriod, new KeelLogger());
    }

    public static void main(String[] args) {
        Keel.initializeVertx(new VertxOptions().setWorkerPoolSize(2));
        Keel.loadPropertiesFromFile("test.sample.properties");
        new KeelServantSerialQueueTest(3000L).run();
    }

    @Override
    protected KeelLogger getLogger() {
        return super.getLogger().setLowestLevel(KeelLogLevel.DEBUG);
    }

    @Override
    public Future<KeelServantQueueTask> getNextTask() {
        int task_id = random.nextInt();
        if (task_id > 200000000) {
            return Future.failedFuture("Cannot get next task !!!");
        }
        if (task_id % 6 != 0) {
            SampleQueueTask task = new SampleQueueTask(task_id);
            return Future.succeededFuture(task);
        } else {
            return Future.failedFuture("Cannot get next task");
        }
    }
}
