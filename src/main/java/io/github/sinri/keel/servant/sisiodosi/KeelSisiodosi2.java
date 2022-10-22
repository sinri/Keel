package io.github.sinri.keel.servant.sisiodosi;

import io.github.sinri.keel.Keel;
import io.github.sinri.keel.core.logger.KeelLogger;
import io.vertx.core.Future;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

/**
 * @since 2.9
 * Experimental
 */
@Deprecated(since = "2.9", forRemoval = true)
public class KeelSisiodosi2 {
    private final Queue<Supplier<Future<Void>>> drops = new ConcurrentLinkedQueue<>();
    private final String lockName;
    private final long minRestMs;
    private final long maxRestMs;
    private final AtomicLong restMsRef;
    private final AtomicLong timerRef = new AtomicLong();
    private KeelLogger logger;

    public KeelSisiodosi2(String lockName) {
        this(lockName, 10L, 1L, 1000L, KeelLogger.silentLogger());
    }

    public KeelSisiodosi2(String lockName, long initRestMs, long minRestMs, long maxRestMs, KeelLogger logger) {
        this.lockName = lockName;
        this.logger = logger;

        restMsRef = new AtomicLong(initRestMs);
        this.minRestMs = minRestMs;
        this.maxRestMs = maxRestMs;

        start();
    }

    public KeelSisiodosi2 setLogger(KeelLogger logger) {
        this.logger = logger;
        return this;
    }

    public void start() {
        if (timerRef.get() != 0) {
            // running!
            return;
        }
        var t = Keel.getVertx().setTimer(restMsRef.get(), timerID -> {
            timerRef.set(0);
            // handle one drop
            Keel.executeWithinLock(lockName, () -> Future.succeededFuture(drops.poll()))
                    .compose(drop -> {
                        if (drop == null) {
                            restMsRef.set(Math.min(this.maxRestMs, restMsRef.get() * 2));
                            this.logger.debug("restMsRef *2 -> " + restMsRef.get());
                            return Future.failedFuture(new NullPointerException());
                        }
                        return drop.get();
                    })
                    .onComplete(ar -> {
                        start();
                    });
        });
        timerRef.set(t);
    }

    public void drop(Supplier<Future<Void>> drip) {
        drops.add(drip);
        restMsRef.set(Math.max(this.minRestMs, restMsRef.get() / 2));
        this.logger.debug("restMsRef /2 -> " + restMsRef.get());
        start();
    }

    public Integer estimateDripQueueSize() {
        return drops.size();
    }
}
