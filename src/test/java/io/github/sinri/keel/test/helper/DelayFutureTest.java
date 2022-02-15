package io.github.sinri.keel.test.helper;


import io.github.sinri.keel.Keel;
import io.github.sinri.keel.core.logger.KeelLogger;
import io.vertx.core.Future;
import io.vertx.core.VertxOptions;

public class DelayFutureTest {
    public static void main(String[] args) {
        Keel.initializeVertx(new VertxOptions().setWorkerPoolSize(2));
        Keel.loadPropertiesFromFile("test.sample.properties");

        KeelLogger logger = Keel.outputLogger("DelayFutureTest");
        logger.info("go");
        Future<Object> future = Future.succeededFuture("a")
                .compose(x -> {
                    logger.info("before " + x);
                    return Future.succeededFuture("b");
                })
                .compose(x -> Keel.getVertx().executeBlocking(promise -> {
                    logger.info("start sleep " + x);
                    try {
                        Thread.sleep(1000L);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    logger.info("end sleep " + x);
                    promise.complete("b");
                }))
                .compose(x -> {
                    logger.info("after " + x);
                    return Future.succeededFuture();
                });
        for (int i = 0; i < 10; i++) {
            int finalI = i;
            future = future.compose(x -> {
                logger.info("after " + x);
                return Future.succeededFuture(finalI);
            });
        }
        future.eventually(v -> {
            logger.info("end" + v);
            Keel.getVertx().close();
            return Future.succeededFuture();
        });
    }
}
