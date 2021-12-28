package io.github.sinri.keel.test.servant.parallel;

import io.github.sinri.keel.Keel;
import io.github.sinri.keel.core.logger.KeelLogger;
import io.github.sinri.keel.servant.KeelServantParallelQueue;
import io.github.sinri.keel.servant.KeelServantQueueTask;
import io.github.sinri.keel.test.servant.SampleQueueTask;
import io.vertx.core.Future;
import io.vertx.core.VertxOptions;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class KeelServantParallelQueueTest extends KeelServantParallelQueue {
    static Random random = new Random();

    public KeelServantParallelQueueTest(long sleepPeriod, int maxParallelSize) {
        super(sleepPeriod, maxParallelSize, new KeelLogger());
    }

    public static void main(String[] args) {
        Keel.initializeVertx(new VertxOptions().setWorkerPoolSize(2));
        Keel.loadPropertiesFromFile("test.properties");
        new KeelServantParallelQueueTest(3000L, 3).run();
    }

    @Override
    public Future<List<KeelServantQueueTask>> getNextTasks(long limit) {
        List<KeelServantQueueTask> list = new ArrayList<>();

        for (long i = 0; i < limit; i++) {
            int task_id = random.nextInt();
            if (task_id % 6 != 0) {
                SampleQueueTask task = new SampleQueueTask(task_id);
                list.add(task);
            } else {
                break;
            }
        }
        if (list.isEmpty()) {
            return Future.failedFuture("Cannot get any task");
        }
        return Future.succeededFuture(list);

    }
}
