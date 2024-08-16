package io.github.sinri.keel.test.lab.cache;

import io.github.sinri.keel.cache.KeelEverlastingCacheVerticle;
import io.github.sinri.keel.facade.async.KeelAsyncKit;
import io.github.sinri.keel.logger.KeelLogLevel;
import io.github.sinri.keel.tesuto.KeelTest;
import io.github.sinri.keel.tesuto.TestUnit;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.ThreadingModel;

import javax.annotation.Nonnull;
import java.util.Date;

import static io.github.sinri.keel.facade.KeelInstance.Keel;

public class KeelCacheDaletTest extends KeelTest {

    @Nonnull
    @Override
    protected Future<Void> starting() {
        Keel.getLogger().setVisibleLevel(KeelLogLevel.DEBUG);
        return super.starting();
    }

    @TestUnit
    public Future<Void> test1() {
        Dalet dalet = new Dalet();
        return dalet.deployMe(new DeploymentOptions().setThreadingModel(ThreadingModel.WORKER))
                .compose(deploymentId -> {
                    getLogger().notice("deployment id: " + deploymentId);
                    return Future.succeededFuture();
                }, throwable -> {
                    getLogger().exception(throwable);
                    return Future.failedFuture(throwable);
                })
                .compose(v -> {
                    return KeelAsyncKit.stepwiseCall(10, i -> {
                        getLogger().info("[" + i + "] " + dalet.read("last_cache_time"));
                        return KeelAsyncKit.sleep(1000L);
                    });
                })
                .compose(v -> {
                    return dalet.undeployMe()
                            .compose(undeployed -> {
                                getLogger().notice("undeployed");
                                return Future.succeededFuture();
                            }, throwable -> {
                                getLogger().exception(throwable);
                                return Future.failedFuture(throwable);
                            });
                })
                .compose(v -> {
                    return KeelAsyncKit.stepwiseCall(10, i -> {
                        getLogger().info("[" + i + "] " + dalet.read("last_cache_time"));
                        return KeelAsyncKit.sleep(1000L);
                    });
                });
    }

    private static class Dalet extends KeelEverlastingCacheVerticle {

        @Override
        public Future<Void> fullyUpdate() {
            this.save("last_cache_time", String.valueOf(System.currentTimeMillis()));
            this.save("last_cache_date", new Date().toString());
            Keel.getLogger().info("updated cache");
            return Future.succeededFuture();
        }

        @Override
        protected long regularUpdatePeriod() {
            return 3_000L;
        }
    }
}
