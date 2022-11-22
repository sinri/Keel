package io.github.sinri.keel.maids.gatling;

import io.github.sinri.keel.Keel;
import io.github.sinri.keel.core.logger.KeelLogger;
import io.github.sinri.keel.verticles.KeelVerticleInterface;
import io.vertx.core.*;
import io.vertx.core.shareddata.Counter;

import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Gatling Gun with multi barrels, for parallel tasks in clustered vertx runtime.
 *
 * @since 2.9.1
 * @since 2.9.3 change to VERTICLE
 */
public class KeelGatling extends AbstractVerticle implements KeelVerticleInterface {
    private final KeelGatlingOptions options;
    private final AtomicInteger barrelUsed = new AtomicInteger(0);

    private KeelGatling(KeelGatlingOptions options) {
        this.options = options;
    }

    public static Future<String> deploy(KeelGatlingOptions options) {
        KeelGatling keelGatling = new KeelGatling(options);
        return Keel.getVertx().deployVerticle(keelGatling, new DeploymentOptions().setWorker(true));
    }

    public KeelLogger getLogger() {
        return options.getLogger();
    }

    public void setLogger(KeelLogger logger) {
        this.options.setLogger(logger);
    }

    protected Future<Void> rest() {
        int actualRestInterval = new Random().nextInt(
                Math.toIntExact(options.getAverageRestInterval() / 2)
        ) + options.getAverageRestInterval();
        return Keel.callFutureSleep(actualRestInterval);
    }

    @Override
    public void start() throws Exception {
        barrelUsed.set(0);
        Keel.callFutureUntil(() -> fireOnce().compose(v -> Future.succeededFuture(false)));
    }

    private Future<Void> fireOnce() {
        if (barrelUsed.get() >= options.getBarrels()) {
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
                .getLock("KeelGatling-" + this.options.getGatlingName() + "-Load")
                .compose(lock -> this.options.getBulletLoader().get().andThen(ar -> lock.release()));
    }

    protected Future<Void> requireExclusiveLocksOfBullet(Bullet bullet) {
        if (bullet.exclusiveLockSet() != null && !bullet.exclusiveLockSet().isEmpty()) {
            AtomicBoolean blocked = new AtomicBoolean(false);
            return Keel.callFutureForEach(
                            bullet.exclusiveLockSet(),
                            exclusiveLock -> {
                                String exclusiveLockName = "KeelGatling-Bullet-Exclusive-Lock-" + exclusiveLock;
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
                String exclusiveLockName = "KeelGatling-Bullet-Exclusive-Lock-" + exclusiveLock;
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
