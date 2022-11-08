package io.github.sinri.keel.maids.maxim;

import io.github.sinri.keel.Keel;
import io.github.sinri.keel.core.logger.KeelLogger;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.shareddata.Counter;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

/**
 * @since 2.9.1
 */
public class KeelMaxim {
    private final String maximUUID;
    private Supplier<Future<Bullet>> bulletLoader;
    private KeelLogger logger;
    private final AtomicInteger barrelUsed = new AtomicInteger(0);
    private int barrels = 1;
    private int averageRestInterval = 1000;

    public KeelMaxim() {
        this.maximUUID = UUID.randomUUID().toString();
        this.logger = Keel.outputLogger();
        this.bulletLoader = () -> Future.succeededFuture(null);
    }

    public KeelMaxim setBarrels(int barrels) {
        this.barrels = barrels;
        return this;
    }

    public KeelMaxim setAverageRestInterval(int averageRestInterval) {
        this.averageRestInterval = averageRestInterval;
        return this;
    }

    public KeelLogger getLogger() {
        return logger;
    }

    public KeelMaxim setLogger(KeelLogger logger) {
        this.logger = logger;
        return this;
    }

    protected Future<Void> rest() {
        int actualRestInterval = new Random().nextInt(Math.toIntExact(averageRestInterval / 2)) + averageRestInterval;
        return Keel.callFutureSleep(actualRestInterval);
    }

    public void startFire() {
        barrelUsed.set(0);
        Keel.callFutureUntil(() -> fireOnce().compose(v -> Future.succeededFuture(false)));
    }

    private Future<Void> fireOnce() {
        if (barrelUsed.get() >= barrels) {
            getLogger().debug("BARREL FULL");
            return rest();
        }
        return Future.succeededFuture()
                .compose(v -> loadOneBullet())
                .compose(bullet -> {
                    if (bullet == null) {
                        return rest();
                    }

                    barrelUsed.incrementAndGet();

                    fireBullet(bullet, firedAR -> {
                        if (firedAR.failed()) {
                            getLogger().exception("BULLET FIRED ERROR", firedAR.cause());
                        } else {
                            getLogger().info("BULLET FIRED DONE");
                        }
                        barrelUsed.decrementAndGet();
                    });

                    return Keel.callFutureSleep(10L);
                })
                .recover(throwable -> {
                    getLogger().exception("FAILED TO LOAD BULLET", throwable);
                    return rest();
                });
    }

    /**
     * Seek one bullet from anywhere with a certain rule.
     *
     * @return Future of a runnable bullet, or null.
     */
    private Future<Bullet> loadOneBullet() {
        return Keel.getVertx().sharedData()
                .getLock("KeelMaxim-" + this.maximUUID + "-Load")
                .compose(lock -> bulletLoader.get().andThen(ar -> lock.release()));
    }

    /**
     * The bullet loader, to load a bullet and ensure the bullet would not be reloaded.
     *
     * @param bulletLoader the supplier to provide a bullet future, and confirm the bullet would not be loaded by any other loader.
     */
    public KeelMaxim setBulletLoader(Supplier<Future<Bullet>> bulletLoader) {
        this.bulletLoader = bulletLoader;
        return this;
    }

    protected Future<Void> requireExclusiveLocksOfBullet(Bullet bullet) {
        if (bullet.exclusiveLockSet() != null && !bullet.exclusiveLockSet().isEmpty()) {
            AtomicBoolean blocked = new AtomicBoolean(false);
            return Keel.callFutureForEach(
                            bullet.exclusiveLockSet(),
                            exclusiveLock -> {
                                String exclusiveLockName = "KeelMaxim-Bullet-Exclusive-Lock-" + exclusiveLock;
                                return Keel.getVertx().sharedData()
                                        .getCounter(exclusiveLockName)
                                        .compose(Counter::incrementAndGet)
                                        .compose(increased -> {
                                            if (increased > 1) {
                                                blocked.set(true);
                                            }
                                            return Future.succeededFuture();
                                        });
                            })
                    .compose(v -> {
                        if (blocked.get()) {
                            return releaseExclusiveLocksOfBullet(bullet)
                                    .eventually(released -> Future.failedFuture(new Exception("This bullet met Exclusive Lock Block.")));
                        }
                        return Future.succeededFuture();
                    });
        } else {
            return Future.succeededFuture();
        }
    }

    protected Future<Void> releaseExclusiveLocksOfBullet(Bullet bullet) {
        if (bullet.exclusiveLockSet() != null && !bullet.exclusiveLockSet().isEmpty()) {
            return Keel.callFutureForEach(bullet.exclusiveLockSet(), exclusiveLock -> {
                String exclusiveLockName = "KeelMaxim-Bullet-Exclusive-Lock-" + exclusiveLock;
                return Keel.getVertx().sharedData().getCounter(exclusiveLockName)
                        .compose(counter -> counter.decrementAndGet()
                                .compose(x -> Future.succeededFuture()));
            });
        } else {
            return Future.succeededFuture();
        }
    }

    private void fireBullet(Bullet bullet, Handler<AsyncResult<Void>> handler) {
        Promise<Void> promise = Promise.promise();
        Future.succeededFuture()
                .compose(v -> requireExclusiveLocksOfBullet(bullet)
                        .compose(locked -> bullet.fire()
                                .andThen(fired -> releaseExclusiveLocksOfBullet(bullet)))
                )
                .andThen(firedAR -> bullet.ejectShell(firedAR)
                        .onComplete(ejected -> {
                            if (firedAR.failed()) {
                                promise.fail(firedAR.cause());
                            } else {
                                promise.complete();
                            }
                        })
                );

        promise.future().andThen(handler);
    }
}
