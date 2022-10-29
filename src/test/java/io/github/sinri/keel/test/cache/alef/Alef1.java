package io.github.sinri.keel.test.cache.alef;

import io.github.sinri.keel.Keel;
import io.github.sinri.keel.core.logger.KeelLogger;
import io.github.sinri.keel.verticles.KeelVerticleInterface;
import io.vertx.core.AbstractVerticle;

import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

public class Alef1 extends AbstractVerticle implements KeelVerticleInterface {
    private static final String KEY = "k";

    public Alef1(KeelLogger logger) {
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
                Long c = KeelCacheAlefTest.alef.read(KEY);
                if (c == null) {
                    KeelCacheAlefTest.alef.save(KEY, now, 2L);
                    getLogger().notice("SAVE " + now);
                } else {
                    getLogger().info("READ " + c);
                }
            }
        });
    }

    private KeelLogger logger;

    @Override
    public KeelLogger getLogger() {
        return logger;
    }

    @Override
    public void setLogger(KeelLogger logger) {
        this.logger = logger;
    }
}
