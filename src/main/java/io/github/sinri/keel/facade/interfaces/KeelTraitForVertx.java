package io.github.sinri.keel.facade.interfaces;

import io.vertx.core.*;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.file.FileSystem;
import io.vertx.core.shareddata.SharedData;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

public interface KeelTraitForVertx {
    @NotNull Vertx getVertx();

    boolean isVertxInitialized();


    @Nonnull
    default EventBus eventBus() {
        return getVertx().eventBus();
    }

    @Nonnull
    default SharedData sharedData() {
        return getVertx().sharedData();
    }

    default long setTimer(long delay, Handler<Long> handler) {
        return getVertx().setTimer(delay, handler);
    }

    default long setPeriodic(long delay, Handler<Long> handler) {
        return setPeriodic(delay, delay, handler);
    }

    default long setPeriodic(long initialDelay, long delay, Handler<Long> handler) {
        return setPeriodic(initialDelay, delay, handler);
    }


    default boolean cancelTimer(long id) {
        return getVertx().cancelTimer(id);
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

    Future<Void> initializeVertx(VertxOptions vertxOptions);

    default Future<Void> initializeVertx(Handler<VertxOptions> vertxOptionsHandler) {
        VertxOptions vertxOptions = new VertxOptions();
        vertxOptionsHandler.handle(vertxOptions);
        return initializeVertx(vertxOptions);
    }


    void addClosePrepareHandler(@Nonnull Handler<Promise<Void>> closePrepareHandler);

    @Nonnull
    List<Handler<Promise<Void>>> getClosePrepareHandlers();

    /**
     * @param gracefulHandler what to do before close vertx
     * @since 2.9.4
     */
    void gracefullyClose(Handler<Promise<Object>> gracefulHandler, Handler<AsyncResult<Void>> vertxCloseHandler);

    default Future<Void> gracefullyClose(Handler<Promise<Object>> gracefulHandler) {
        Promise<Void> vertxClosed = Promise.promise();
        gracefullyClose(gracefulHandler, vertxClosed);
        return vertxClosed.future();
    }

    default Future<Void> gracefullyClose() {
        return gracefullyClose(Promise::complete);
    }

    default FileSystem fileSystem() {
        return getVertx().fileSystem();
    }

    default Future<Void> undeploy(String deploymentID) {
        return getVertx().undeploy(deploymentID);
    }

    default void undeploy(String deploymentID, Handler<AsyncResult<Void>> completionHandler) {
        getVertx().undeploy(deploymentID, completionHandler);
    }

    default Set<String> deploymentIDs() {
        return getVertx().deploymentIDs();
    }
}
