package io.github.sinri.keel.maids.maxim;

import io.github.sinri.keel.Keel;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.shareddata.Lock;

import java.util.Set;

abstract public class Bullet {
    abstract public String bulletID();

    abstract protected Set<String> exclusiveLockSet();

    public void lockAndFire(Promise<Void> promise) {
        String lockName = "KeelMaxim-Bullet-" + bulletID();
        Keel.getVertx().sharedData().getLock(lockName, lockAR -> {
            if (lockAR.failed()) {
                promise.fail(lockAR.cause());
            } else {
                Lock lock = lockAR.result();
                Future.succeededFuture()
                        .compose(locked -> {
                            return Future.succeededFuture()
                                    .compose(v -> {
                                        if (exclusiveLockSet() != null && !exclusiveLockSet().isEmpty()) {
                                            return Keel.callFutureForEach(exclusiveLockSet(), exclusiveLock -> {
                                                String exclusiveLockName = "KeelMaxim-Bullet-Exclusive-Lock-" + exclusiveLock;
                                                return Keel.getVertx().sharedData().getCounter(exclusiveLockName)
                                                        .compose(counter -> {
                                                            return counter.incrementAndGet()
                                                                    .compose(increased -> {
                                                                        if (increased > 1) {
                                                                            return counter.decrementAndGet()
                                                                                    .compose(decreased -> Future.failedFuture(new Exception()));
                                                                        }
                                                                        return Future.succeededFuture();
                                                                    });
                                                        });
                                            });
                                        } else {
                                            return Future.succeededFuture();
                                        }
                                    })
                                    .compose(v -> {
                                        return fire();
                                    })
                                    .eventually(v -> {
                                        if (exclusiveLockSet() != null && !exclusiveLockSet().isEmpty()) {
                                            return Keel.callFutureForEach(exclusiveLockSet(), exclusiveLock -> {
                                                String exclusiveLockName = "KeelMaxim-Bullet-Exclusive-Lock-" + exclusiveLock;
                                                return Keel.getVertx().sharedData().getCounter(exclusiveLockName)
                                                        .compose(counter -> {
                                                            return counter.decrementAndGet()
                                                                    .compose(x -> {
                                                                        return Future.succeededFuture();
                                                                    });
                                                        });
                                            });
                                        } else {
                                            return Future.succeededFuture();
                                        }
                                    });
                        })
                        .andThen(firedAR -> {
                            lock.release();
                            if (firedAR.failed()) {
                                promise.fail(firedAR.cause());
                            } else {
                                promise.complete();
                            }
                        });
            }
        });
    }

    abstract protected Future<Void> fire();
}
