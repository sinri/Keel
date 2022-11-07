package io.github.sinri.keel.maids.maxim;

import io.github.sinri.keel.Keel;
import io.github.sinri.keel.core.logger.KeelLogger;
import io.vertx.core.Future;
import io.vertx.core.Promise;

import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

public class KeelMaxim {
    private final String maximUUID;
    private Supplier<Future<Bullet>> bulletLoader;
    private KeelLogger logger;
    private long restInterval = 1000L;
    private int barrels = 1;

    public KeelMaxim() {
        this.maximUUID = UUID.randomUUID().toString();
        this.logger = Keel.outputLogger();
        this.bulletLoader = () -> Future.succeededFuture(null);
    }

    public KeelMaxim setBarrels(int barrels) {
        this.barrels = barrels;
        return this;
    }

    public KeelMaxim setRestInterval(long restInterval) {
        this.restInterval = restInterval;
        return this;
    }

    public KeelLogger getLogger() {
        return logger;
    }

    public KeelMaxim setLogger(KeelLogger logger) {
        this.logger = logger;
        return this;
    }

    public void fire() {
        AtomicInteger barrelUsed = new AtomicInteger(0);

        Keel.callFutureUntil(() -> Future.succeededFuture()
                .compose(v -> {
                    if (barrelUsed.get() >= barrels) {
                        getLogger().debug("BARREL FULL");
                        return Keel.callFutureSleep(restInterval);
                    }
                    return load().compose(bullet -> {
                                Objects.requireNonNull(bullet);

                                barrelUsed.incrementAndGet();

                                Promise<Void> promise = Promise.promise();
                                bullet.lockAndFire(promise);

                                promise.future().andThen(firedAR -> {
                                    if (firedAR.failed()) {
                                        getLogger().exception("BULLET FIRED ERROR", firedAR.cause());
                                    } else {
                                        getLogger().info("BULLET FIRED DONE");
                                    }
                                    barrelUsed.decrementAndGet();
                                });

                                return Future.succeededFuture();
                            }, throwable -> {
                                getLogger().exception("FAILED TO LOAD BULLET", throwable);
                                return Future.succeededFuture();
                            })
                            .compose(over -> {
                                return Keel.callFutureSleep(100L);
                            }, throwable -> {
                                getLogger().exception(throwable);
                                return Keel.callFutureSleep(restInterval);
                            });
                })
                .compose(v -> {
                    return Future.succeededFuture(false);
                }));
    }

    /**
     * Seek one bullet from anywhere with a certain rule.
     *
     * @return Future of a runnable bullet, or null.
     */
    private Future<Bullet> load() {
        return bulletLoader.get();
    }

    public KeelMaxim setBulletLoader(Supplier<Future<Bullet>> bulletLoader) {
        this.bulletLoader = bulletLoader;
        return this;
    }
}
