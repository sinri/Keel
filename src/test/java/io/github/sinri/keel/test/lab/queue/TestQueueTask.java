package io.github.sinri.keel.test.lab.queue;

import io.github.sinri.keel.facade.async.KeelAsyncKit;
import io.github.sinri.keel.logger.event.KeelEventLogger;
import io.github.sinri.keel.logger.event.center.KeelOutputEventLogCenter;
import io.github.sinri.keel.servant.queue.KeelQueueTask;
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

    @Override
    protected KeelEventLogger prepareLogger() {
        return KeelOutputEventLogCenter.getInstance().createLogger("TestQueue", log -> log
                .put("id", id)
                .put("life", life)
        );
    }

    @Override
    protected Future<Void> run() {
        getLogger().info("START");
        return KeelAsyncKit.sleep(this.life * 1000L)
                .eventually(v -> {
                    getLogger().info("END ");
                    return Future.succeededFuture();
                });
    }
}
