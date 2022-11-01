package io.github.sinri.keel.test.hazelcast;

import io.github.sinri.keel.Keel;
import io.github.sinri.keel.core.logger.KeelLogger;
import io.github.sinri.keel.servant.maxim.KeelMaxim;
import io.vertx.core.Future;

public class C1 {
    static KeelLogger logger;

    public static void main(String[] args) {
        KeelLogger logger = Keel.outputLogger("C1-Maxim");
        Cluster.startCluster(14001)
                .compose(init -> {
                    logger.info("14001 GO");

                    maximConsumer();

                    return Future.succeededFuture();
                })
                .onFailure(throwable -> {
                    logger.exception("!!!", throwable);
                });
    }

    private static void maximConsumer() {
        KeelMaxim keelMaxim = new KeelMaxim("1400x");

        keelMaxim.setLogger(logger);
        keelMaxim.runAsConsumer();
        keelMaxim.runAsProducer();
    }
}
