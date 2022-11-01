package io.github.sinri.keel.test.hazelcast;

import io.github.sinri.keel.Keel;
import io.github.sinri.keel.core.logger.KeelLogger;
import io.vertx.core.Future;

import java.util.UUID;
import java.util.function.Function;

public class C2 {
    static KeelLogger logger;

    public static void main(String[] args) {
        logger = Keel.outputLogger("C2-Maxim");
        Cluster.startCluster(14002)
                .compose(init -> {
                    logger.info("14002 GO");

                    return maximProducer();
                })
                .onFailure(throwable -> {
                    logger.exception("!!!", throwable);
                });
    }

    private static Future<Void> maximProducer() {
        //KeelMaxim keelMaxim = new KeelMaxim("1400x");
        //keelMaxim.setLogger(logger);
        //keelMaxim.runAsProducer();

        return Keel.callFutureForRange(100, new Function<Integer, Future<Void>>() {
                    @Override
                    public Future<Void> apply(Integer integer) {
                        BulletA bulletA = new BulletA();
                        bulletA.setID(UUID.randomUUID().toString());
                        bulletA.setX(integer % 5 == 0 ? null : String.valueOf(integer));
                        Keel.getVertx().eventBus().send("1400x", bulletA.toJsonObject());
                        logger.info("SEND " + bulletA);
                        return Keel.callFutureSleep(100L);
                    }
                })
                .onFailure(throwable -> {
                    logger.exception("!!!", throwable);
                });
    }
}
