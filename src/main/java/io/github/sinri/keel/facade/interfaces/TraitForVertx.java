package io.github.sinri.keel.facade.interfaces;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.shareddata.SharedData;
import io.vertx.core.spi.cluster.ClusterManager;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Supplier;

public interface TraitForVertx {
    @NotNull Vertx getVertx();

    @Nullable
    ClusterManager getClusterManager();

    default @Nonnull EventBus eventBus() {
        return getVertx().eventBus();
    }

    default @Nonnull SharedData sharedData() {
        return getVertx().sharedData();
    }


    /**
     * @since 2.9
     */
    default <R> Future<R> executeWithinLock(String lockName, Supplier<Future<R>> supplier) {
        return executeWithinLock(lockName, 10_000L, supplier);
    }

    /**
     * @since 2.9
     */
    default <R> Future<R> executeWithinLock(String lockName, long timeout, Supplier<Future<R>> supplier) {
        return sharedData().getLockWithTimeout(lockName, timeout)
                .compose(lock -> Future.succeededFuture()
                        .compose(v -> supplier.get())
                        .onComplete(ar -> lock.release()));
    }


}
