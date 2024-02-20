package io.github.sinri.keel.test.lab.queue;

import io.github.sinri.keel.facade.async.KeelAsyncKit;
import io.github.sinri.keel.logger.KeelLogLevel;
import io.github.sinri.keel.logger.event.KeelEventLogToBeExtended;
import io.github.sinri.keel.logger.event.KeelEventLogger;
import io.github.sinri.keel.logger.event.center.KeelOutputEventLogCenter;
import io.github.sinri.keel.servant.queue.KeelQueueTask;
import io.vertx.core.Future;

import java.util.function.Supplier;

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
        KeelEventLogger logger = KeelOutputEventLogCenter.getInstance().createLogger("TestQueue");
        logger.setBaseLogBuilder(new Supplier<KeelEventLogToBeExtended>() {
            @Override
            public KeelEventLogToBeExtended get() {
                return new KeelEventLogToBeExtended(KeelLogLevel.INFO, logger.getPresetTopic()) {
                    @Override
                    protected void prepare() {
                        this.context(c -> c
                                .put("id", id)
                                .put("life", life)
                        );
                    }
                };
            }
        });
        return logger;
    }

    @Override
    protected Future<Void> run() {
        getLogger().info("START");
        return KeelAsyncKit.sleep(this.life * 1000L)
                .eventually(() -> {
                    getLogger().info("END ");
                    return Future.succeededFuture();
                });
    }
}
