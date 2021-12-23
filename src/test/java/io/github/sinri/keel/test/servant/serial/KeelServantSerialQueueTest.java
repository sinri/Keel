package io.github.sinri.keel.test.servant.serial;

import io.github.sinri.keel.servant.KeelServantQueueTask;
import io.github.sinri.keel.servant.KeelServantSerialQueue;
import io.vertx.core.Future;

import java.util.Random;

public class KeelServantSerialQueueTest extends KeelServantSerialQueue {
    static Random random = new Random();

    public static void main(String[] args) {
        new KeelServantSerialQueueTest().run();
    }

    @Override
    public Future<String> whenExecuteFailed(Throwable throwable) {
        System.out.println("KeelServantSerialQueueTest whenExecuteFailed " + throwable);
        return Future.succeededFuture(null);
    }

    @Override
    public Future<String> whenExecuteDone(String feedback) {
        System.out.println("KeelServantSerialQueueTest whenExecuteDone " + feedback);
        return Future.succeededFuture(null);
    }

    @Override
    public Future<KeelServantQueueTask> getNextTask() {
        int task_id = random.nextInt();
        if (task_id % 6 != 0) {
            SampleQueueTask task = new SampleQueueTask(task_id);
            return Future.succeededFuture(task);
        } else {
            return Future.failedFuture("Cannot get next task");
        }
    }
}
