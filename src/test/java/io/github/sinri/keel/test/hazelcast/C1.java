package io.github.sinri.keel.test.hazelcast;

import io.github.sinri.keel.Keel;
import io.github.sinri.keel.core.logger.KeelLogger;
import io.vertx.core.Future;

public class C1 {
    static KeelLogger logger;

    public static void main(String[] args) {
        Cluster.startCluster()
                .compose(init -> {
                    logger = Keel.outputLogger("C1-Maxim");
                    logger.info("14001 GO");

                    Keel.getVertx().eventBus().consumer("1400x")
                            .handler(message -> {
                                Object body = message.body();
                                Keel.outputLogger().info("message received: " + body.toString());
                            });

                    return Future.succeededFuture();
                })
                .onFailure(throwable -> {
                    Keel.outputLogger().exception("!!!", throwable);
                });
    }
}
