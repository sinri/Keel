package io.github.sinri.keel.test.hazelcast;

import io.github.sinri.keel.lagecy.Keel;
import io.github.sinri.keel.lagecy.core.logger.KeelLogger;
import io.github.sinri.keel.test.hazelcast.watch.WatchTest;
import io.vertx.core.Future;

public class C1 {
    static KeelLogger logger;

    public static void main(String[] args) {
        Cluster.startCluster()
                .compose(init -> {
                    logger = Keel.outputLogger("C1-Maxim");
                    logger.info("14001 GO");

//                    MessageConsumer<Long> consumer = Keel.getVertx().eventBus().consumer("1400x");
//                    consumer.handler(message -> {
//                        Long body = message.body();
//                        Keel.outputLogger().info("message received: " + body);
//                        long reply = body + 1;
//                        message.reply(reply);
//                    });

//                    TestGatling.startOnClusterNode();
//                    WatchTest.testPure();
                    WatchTest.testCron();

                    return Future.succeededFuture();
                })
                .onFailure(throwable -> {
                    Keel.outputLogger().exception("!!!", throwable);
                });
    }
}
