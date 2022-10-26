package io.github.sinri.keel.servant.funnel;

import io.github.sinri.keel.Keel;
import io.github.sinri.keel.core.controlflow.FutureUntil;
import io.github.sinri.keel.core.logger.KeelLogger;
import io.github.sinri.keel.verticles.KeelVerticleInterface;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;

import java.util.Date;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

/**
 * 随时接收小任务，并周期性轮询依次执行。
 * 在部署为verticle（deploy）时自动开始。
 *
 * @since 2.9 rename from sisiodosi to funnel
 */
public class KeelFunnel extends AbstractVerticle implements KeelVerticleInterface {
    private final Queue<Supplier<Future<Object>>> drips = new ConcurrentLinkedQueue<>();
    private final AtomicLong restingStartTime = new AtomicLong(0);

    private Options options;

    private KeelLogger logger;

    private KeelFunnel() {
        this.logger = KeelLogger.silentLogger();
    }

    public static KeelFunnel getOneInstanceToDeploy(long interval) {
        return getOneInstanceToDeploy(new Options().setQueryInterval(interval));
    }

    public static KeelFunnel getOneInstanceToDeploy(KeelFunnel.Options options) {
        return new KeelFunnel().setOptions(options);
    }

    public static Future<KeelFunnel> deployOneInstance(long interval) {
        return deployOneInstance(new Options().setQueryInterval(interval));
    }

    public static Future<KeelFunnel> deployOneInstance(KeelFunnel.Options options) {
        KeelFunnel keelFunnel = new KeelFunnel().setOptions(options);
        DeploymentOptions deploymentOptions = new DeploymentOptions().setWorker(true);
        return Keel.getVertx().deployVerticle(keelFunnel, deploymentOptions)
                .compose(d -> Future.succeededFuture(keelFunnel));
    }

    @Override
    public KeelLogger getLogger() {
        return logger;
    }

//    @Override
//    public long getTimeThreshold() {
//        return options.getTimeThreshold();
//    }

//    @Override
//    public int getSizeThreshold() {
//        return options.getSizeThreshold();
//    }

    @Override
    public void setLogger(KeelLogger logger) {
        this.logger = logger;
    }

    public KeelFunnel setOptions(Options options) {
        this.options = options;
        return this;
    }

    public int getTotalDrips() {
        return drips.size();
    }

    public long getQueryInterval() {
        return options.getQueryInterval();
    }

    public void drop(Supplier<Future<Object>> drip) {
        if (this.deploymentID() == null) {
            throw new RuntimeException("NOT DEPLOYED");
        }
        drips.add(drip);
    }

    private void query() {
        if (drips.size() > 0) {
//            if (drips.size() >= getSizeThreshold()) {
//                getLogger().debug("DRIPS " + drips.size() + " >= Size Threshold " + getSizeThreshold());
//                pour();
//                return;
//            }

//            long restedTime = new Date().getTime() - restingStartTime.get();
//            if (restedTime > getTimeThreshold()) {
//                getLogger().debug("RESTED TIME " + restedTime + " >= Time Threshold " + getTimeThreshold() + " while DRIPS " + drips.size());
//                pour();
//                return;
//            }

            pour();
            return;
        }
        // 暇、続きの暇
        Keel.getVertx().setTimer(getQueryInterval(), timerID -> query());
    }

    private Future<Boolean> pourOneDrip() {
        return Future.succeededFuture(drips.poll())
                .compose(drip -> {
                    if (drip == null) {
                        return Future.succeededFuture(true);
                    } else {
                        return drip.get()
                                .compose(v -> {
                                    getLogger().info("DRIP DONE");
                                    return Future.succeededFuture(false);
                                }, throwable -> {
                                    getLogger().exception("DRIP FAILED", throwable);
                                    return Future.succeededFuture(false);
                                });
                    }
                });
    }

    private void pour() {
        getLogger().debug("POUR START");
        FutureUntil.call(this::pourOneDrip)
                .onComplete(poured -> {
                    getLogger().debug("POUR END");
                    restingStartTime.set(new Date().getTime());
                    query();
                });
    }

    @Override
    public void start() throws Exception {
        super.start();
        restingStartTime.set(new Date().getTime());
        query();
    }

    public static class Options {
        private long queryInterval = 100;
//        private long timeThreshold = 1000;
//        private int sizeThreshold = 10;

        public long getQueryInterval() {
            return queryInterval;
        }

        public Options setQueryInterval(long queryInterval) {
            this.queryInterval = queryInterval;
            return this;
        }

//        public long getTimeThreshold() {
//            return timeThreshold;
//        }
//
//        public Options setTimeThreshold(long timeThreshold) {
//            this.timeThreshold = timeThreshold;
//            return this;
//        }

//        public int getSizeThreshold() {
//            return sizeThreshold;
//        }

//        public Options setSizeThreshold(int sizeThreshold) {
//            this.sizeThreshold = sizeThreshold;
//            return this;
//        }
    }
}
