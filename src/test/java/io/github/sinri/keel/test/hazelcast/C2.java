package io.github.sinri.keel.test.hazelcast;

import io.github.sinri.keel.Keel;
import io.github.sinri.keel.core.logger.KeelLogger;
import io.github.sinri.keel.test.hazelcast.watch.WatchTest;
import io.vertx.core.Future;

public class C2 {
    static KeelLogger logger;

    public static void main(String[] args) {

        Cluster.startCluster()
                .compose(init -> {
                    logger = Keel.outputLogger("C2-Maxim");
                    logger.info("14002 GO");

                    //Keel.getVertx().eventBus().send("1400x", new Date().getTime());
//                    Keel.getVertx().eventBus().request("1400x", new Date().getTime(), messageAsyncResult -> {
//                        if (messageAsyncResult.failed()) {
//                            Keel.outputLogger().exception(messageAsyncResult.cause());
//                        } else {
//                            Message<Object> result = messageAsyncResult.result();
//                            Object body = result.body();
//                            Keel.outputLogger().info("REPLIED: " + body);
//                        }
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

//    private static void maximProducer() {
//        //KeelMaxim keelMaxim = new KeelMaxim("1400x");
//        //keelMaxim.setLogger(logger);
//        //keelMaxim.runAsProducer();
//
//        AtomicInteger i = new AtomicInteger(0);
//        new KeelEndless(1000L, new Supplier<Future<Void>>() {
//            @Override
//            public Future<Void> get() {
//                Keel.getVertx().eventBus().send("1400x", Keel.helpers().datetime().getGMTDateTimeExpression());
//                return Keel.callFutureSleep(100L);
//            }
//        }).start();
//    }
}
