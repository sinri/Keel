package io.github.sinri.keel.maids.gatling;

import io.github.sinri.keel.facade.Keel;
import io.github.sinri.keel.facade.async.KeelAsyncKit;
import io.github.sinri.keel.verticles.KeelVerticleBase;
import io.vertx.core.*;
import io.vertx.core.shareddata.Counter;

import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

/**
 * Gatling Gun with multi barrels, for parallel tasks in clustered vertx runtime.
 *
 * @since 2.9.1
 * @since 2.9.3 change to VERTICLE
 */
public class KeelGatling extends KeelVerticleBase {
    private final Options options;
    private final AtomicInteger barrelUsed = new AtomicInteger(0);

    private KeelGatling(Options options) {
        this.options = options;
    }

    public static Future<String> deploy(String gatlingName, Handler<Options> optionHandler) {
        Options options = new Options(gatlingName);
        optionHandler.handle(options);
        KeelGatling keelGatling = new KeelGatling(options);
        return Keel.getVertx().deployVerticle(keelGatling, new DeploymentOptions().setWorker(true));
    }

    protected Future<Void> rest() {
        int actualRestInterval = new Random().nextInt(
                Math.toIntExact(options.getAverageRestInterval() / 2)
        ) + options.getAverageRestInterval();
        return KeelAsyncKit.sleep(actualRestInterval);
    }

    @Override
    public void start() throws Exception {
        barrelUsed.set(0);
        KeelAsyncKit.repeatedlyCall(routineResult -> {
            return fireOnce();
        });
        //Keel.callFutureUntil(() -> fireOnce().compose(v -> Future.succeededFuture(false)));
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
                            getLogger().exception(firedAR.cause(), "BULLET FIRED ERROR");
                        } else {
                            getLogger().info("BULLET FIRED DONE");
                        }
                        barrelUsed.decrementAndGet();
                    });

                    return KeelAsyncKit.sleep(10L);
                })
                .recover(throwable -> {
                    getLogger().exception(throwable, "FAILED TO LOAD BULLET");
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
            return KeelAsyncKit.iterativelyCall(
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
            return KeelAsyncKit.iterativelyCall(bullet.exclusiveLockSet(), exclusiveLock -> {
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

    public static class Options {
        private final String gatlingName;
        private int barrels;
        private int averageRestInterval;
        private Supplier<Future<Bullet>> bulletLoader;
//        private KeelEventLogger logger;

        public Options(String gatlingName) {
            this.gatlingName = gatlingName;
            this.barrels = 1;
            this.averageRestInterval = 1000;
            this.bulletLoader = () -> Future.succeededFuture(null);
//            this.logger = KeelEventLogger.silentLogger();
        }

        /**
         * @return 加特林机枪名称（集群中各节点之间的识别同一组加特林机枪类的实例用）
         */
        public String getGatlingName() {
            return gatlingName;
        }

        /**
         * @return 枪管数量（并发任务数）
         */
        public int getBarrels() {
            return barrels;
        }

        /**
         * @param barrels 枪管数量（并发任务数）
         */
        public Options setBarrels(int barrels) {
            this.barrels = barrels;
            return this;
        }

        /**
         * @return 弹带更换平均等待时长（没有新任务时的休眠期，单位0.001秒）
         */
        public int getAverageRestInterval() {
            return averageRestInterval;
        }

        /**
         * @param averageRestInterval 弹带更换平均等待时长（没有新任务时的休眠期，单位0.001秒）
         */
        public Options setAverageRestInterval(int averageRestInterval) {
            this.averageRestInterval = averageRestInterval;
            return this;
        }

        /**
         * @return 供弹器（新任务生成器）
         */
        public Supplier<Future<Bullet>> getBulletLoader() {
            return bulletLoader;
        }

        /**
         * @param bulletLoader 供弹器（新任务生成器）
         */
        public Options setBulletLoader(Supplier<Future<Bullet>> bulletLoader) {
            this.bulletLoader = bulletLoader;
            return this;
        }

//        /**
//         * @return 日志记录仪
//         */
//        public KeelEventLogger getLogger() {
//            return logger;
//        }
//
//        /**
//         * @param logger 日志记录仪
//         */
//        public Options setLogger(KeelEventLogger logger) {
//            this.logger = logger;
//            return this;
//        }


    }
}
