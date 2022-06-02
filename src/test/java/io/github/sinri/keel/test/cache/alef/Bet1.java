package io.github.sinri.keel.test.cache.alef;

import io.github.sinri.keel.Keel;
import io.github.sinri.keel.core.logger.KeelLogger;
import io.github.sinri.keel.verticles.KeelVerticle;
import io.vertx.core.Future;

import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

public class Bet1 extends KeelVerticle {
    private static final String KEY = "k";

    public Bet1(KeelLogger logger) {
        setLogger(logger);
    }

    @Override
    public void start() throws Exception {
        super.start();

        AtomicInteger atomicInteger = new AtomicInteger(0);

        long delay = (long) (300L + (Math.random() * 200));
        getLogger().info("DELAY IS " + delay);

        Keel.getVertx().setPeriodic(delay, timerID -> {
            if (atomicInteger.get() > 10) {
                Keel.getVertx().cancelTimer(timerID);
                undeployMe();
            } else {
                long now = new Date().getTime();

                KeelCacheBetTest.bet.read(KEY)
                        .compose(c -> {
                            getLogger().info("READ " + c);
                            return Future.succeededFuture();
                        })
                        .otherwise(throwable -> {
                            KeelCacheBetTest.bet.save(KEY, now, 2L);
                            getLogger().exception("SAVE " + now, throwable);
                            return Future.succeededFuture();
                        });
            }
        });
    }
}
