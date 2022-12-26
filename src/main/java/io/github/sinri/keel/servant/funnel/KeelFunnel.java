package io.github.sinri.keel.servant.funnel;

import io.github.sinri.keel.facade.Keel3;
import io.github.sinri.keel.facade.async.FutureRepeat;
import io.github.sinri.keel.facade.async.KeelAsyncKit;
import io.github.sinri.keel.logger.event.KeelEventLogger;
import io.github.sinri.keel.verticles.KeelVerticleBase;
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
 * <p>
 * 仅用于单节点模式。
 *
 * @since 2.9 rename from sisiodosi to funnel
 */
public class KeelFunnel extends KeelVerticleBase {
    private final Queue<Supplier<Future<Object>>> drips = new ConcurrentLinkedQueue<>();
    private final AtomicLong restingStartTime = new AtomicLong(0);

    private Options options;

    private final KeelEventLogger logger;

    private KeelFunnel() {
        this.logger = KeelEventLogger.silentLogger();
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
        return Keel3.getVertx().deployVerticle(keelFunnel, deploymentOptions)
                .compose(d -> Future.succeededFuture(keelFunnel));
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
            pour();
            return;
        }
        // 暇、続きの暇
        Keel3.getVertx().setTimer(getQueryInterval(), timerID -> query());
    }

    private Future<Void> pourOneDrip(FutureRepeat.RoutineResult routineResult) {
        return Future.succeededFuture(drips.poll())
                .compose(drip -> {
                    if (drip == null) {
                        routineResult.stop();
                        return Future.succeededFuture();
                    } else {
                        return drip.get()
                                .compose(v -> {
                                    getLogger().info("DRIP DONE");
                                    return Future.succeededFuture();
                                }, throwable -> {
                                    getLogger().exception(throwable, "DRIP FAILED");
                                    return Future.succeededFuture();
                                });
                    }
                });
    }

    private void pour() {
        getLogger().debug("POUR START");

        KeelAsyncKit.repeatedlyCall(this::pourOneDrip)
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
