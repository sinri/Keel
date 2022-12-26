package io.github.sinri.keel.facade.interfaces;

import io.github.sinri.keel.facade.Keel3;
import io.vertx.core.Future;

import java.util.function.Supplier;

public interface KeelSharingKit {

    static <R> Future<R> executeWithinLock(String lockName, Supplier<Future<R>> supplier) {
        return executeWithinLock(lockName, 10_000L, supplier);
    }


    static <R> Future<R> executeWithinLock(String lockName, long timeout, Supplier<Future<R>> supplier) {
        return Keel3.getVertx().sharedData().getLockWithTimeout(lockName, timeout)
                .compose(lock -> Future.succeededFuture()
                        .compose(v -> supplier.get())
                        .onComplete(ar -> lock.release()));
    }
}
