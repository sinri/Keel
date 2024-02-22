package io.github.sinri.keel.maids.watchman;

import io.github.sinri.keel.logger.event.KeelEventLog;
import io.github.sinri.keel.verticles.KeelVerticleBase;
import io.vertx.core.eventbus.Message;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.shareddata.Lock;

import static io.github.sinri.keel.facade.KeelInstance.Keel;

/**
 * @since 2.9.3
 */
abstract class KeelWatchmanImpl extends KeelVerticleBase<KeelEventLog> implements KeelWatchman<KeelEventLog> {
    private final String watchmanName;
    private MessageConsumer<Long> consumer;

    public KeelWatchmanImpl(String watchmanName) {
        this.watchmanName = watchmanName;
    }

    @Override
    public String watchmanName() {
        return this.watchmanName;
    }

    protected String eventBusAddress() {
        return this.getClass().getName() + ":" + watchmanName();
    }

    @Override
    public void start() {
        this.consumer = Keel.getVertx().eventBus().consumer(eventBusAddress());
        this.consumer.handler(this::consumeHandleMassage);
        this.consumer.exceptionHandler(throwable -> getRoutineIssueRecorder()
                .exception(throwable, r -> r.message(watchmanName() + " ERROR")));

        try {
            // @since 2.9.3 强行拟合HH:MM:SS.000-200
            long x = 1000 - System.currentTimeMillis() % 1_000;
            if (x < 800) {
                Thread.sleep(x);
            }
        } catch (Exception ignore) {
            // 拟合不了拉倒
        }
        Keel.getVertx().setPeriodic(
                interval(),
                timerID -> Keel.getVertx().eventBus()
                        .send(eventBusAddress(), System.currentTimeMillis()));
    }

    protected void consumeHandleMassage(Message<Long> message) {
        Long timestamp = message.body();
        getRoutineIssueRecorder().debug(r -> r.message(watchmanName() + " TRIGGERED FOR " + timestamp));

        long x = timestamp / interval();
        Keel.getVertx().sharedData().getLockWithTimeout(eventBusAddress() + "@" + x, Math.min(3_000L, interval() - 1), lockAR -> {
            if (lockAR.failed()) {
                getRoutineIssueRecorder().warning(r -> r.message("LOCK ACQUIRE FAILED FOR " + timestamp + " i.e. " + x));
            } else {
                Lock lock = lockAR.result();
                getRoutineIssueRecorder().info(r -> r.message("LOCK ACQUIRED FOR " + timestamp + " i.e. " + x));
                regularHandler().handle(timestamp);
                Keel.getVertx().setTimer(interval(), timerID -> {
                    lock.release();
                    getRoutineIssueRecorder().info(r -> r.message("LOCK RELEASED FOR " + timestamp + " i.e. " + x));
                });
            }
        });
    }

    @Override
    public void stop() throws Exception {
        super.stop();
        consumer.unregister();
    }
}
