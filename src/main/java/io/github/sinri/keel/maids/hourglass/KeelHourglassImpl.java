package io.github.sinri.keel.maids.hourglass;

import io.github.sinri.keel.Keel;
import io.github.sinri.keel.core.logger.KeelLogger;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.shareddata.Lock;

/**
 * @since 2.9.3
 */
abstract class KeelHourglassImpl extends AbstractVerticle implements KeelHourglass {
    private KeelLogger logger;
    private MessageConsumer<Long> consumer;
    private final String hourglassName;

    public KeelHourglassImpl(String hourglassName) {
        this.logger = KeelLogger.silentLogger();//Keel.outputLogger(hourglassName);
        this.hourglassName = hourglassName;
    }

    @Override
    public String hourglassName() {
        return this.hourglassName;
    }

    @Override
    public KeelLogger getLogger() {
        return logger;
    }

    @Override
    public void setLogger(KeelLogger logger) {
        this.logger = logger;
    }

    protected String eventBusAddress() {
        return this.getClass().getName() + ":" + hourglassName();
    }

    @Override
    public void start() throws Exception {
        super.start();

        this.consumer = Keel.getVertx().eventBus().consumer(eventBusAddress());
        this.consumer.handler(message -> {
            Long timestamp = message.body();
            getLogger().debug(hourglassName() + " TRIGGERED FOR " + timestamp);

            long x = timestamp / interval();
            Keel.getVertx().sharedData().getLockWithTimeout(eventBusAddress() + "@" + x, Math.min(3_000L, interval() - 1), lockAR -> {
                if (lockAR.failed()) {
                    getLogger().warning("LOCK ACQUIRE FAILED FOR " + timestamp + " i.e. " + x);
                } else {
                    Lock lock = lockAR.result();
                    getLogger().info("LOCK ACQUIRED FOR " + timestamp + " i.e. " + x);
                    regularHandler().handle(timestamp);
                    Keel.getVertx().setTimer(interval(), timerID -> {
                        lock.release();
                        getLogger().info("LOCK RELEASED FOR " + timestamp + " i.e. " + x);
                    });
                }
            });
        });
        this.consumer.exceptionHandler(throwable -> {
            getLogger().exception(hourglassName() + " ERROR", throwable);
        });

        Keel.getVertx().setPeriodic(
                interval(),
                timerID -> Keel.getVertx().eventBus()
                        .send(eventBusAddress(), System.currentTimeMillis()));
    }

    @Override
    public void stop() throws Exception {
        super.stop();
        consumer.unregister();
    }
}
