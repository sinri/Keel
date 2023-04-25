package io.github.sinri.keel.cache.temporaryvalue;

import io.github.sinri.keel.cache.ValueWrapper;
import io.vertx.core.Future;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

/**
 * @since 3.0.1
 */
public class KeelAsyncTemporaryValue<P> {
    private final AtomicReference<ValueWrapper<P>> valueWrapperAtomicReference;
    private long defaultLifetimeInSeconds = 10L;

    public KeelAsyncTemporaryValue() {
        this.valueWrapperAtomicReference = new AtomicReference<>();
    }

    public KeelAsyncTemporaryValue<P> setDefaultLifetimeInSeconds(long defaultLifetimeInSeconds) {
        this.defaultLifetimeInSeconds = defaultLifetimeInSeconds;
        return this;
    }

    public Future<Void> set(P p) {
        return set(p, defaultLifetimeInSeconds);
    }

    public Future<Void> set(P p, long lifeInSeconds) {
        this.valueWrapperAtomicReference.set(new ValueWrapper<>(p, lifeInSeconds));
        return Future.succeededFuture();
    }

    public Future<P> rawGet() {
        return Future.succeededFuture(this.valueWrapperAtomicReference.get())
                .compose(pValueWrapper -> {
                    if (pValueWrapper == null) {
                        throw new ValueInvalidNow();
                    }
                    if (!pValueWrapper.isAliveNow()) {
                        throw new ValueInvalidNow();
                    }
                    return Future.succeededFuture(pValueWrapper.getValue());
                });
    }

    public Future<P> get() {
        return getOrElse(null);
    }

    public Future<P> getOrElse(P fallback) {
        return rawGet().recover(throwable -> {
            return Future.succeededFuture(fallback);
        });
    }

    public Future<P> getOrReload(@NotNull Supplier<Future<P>> loader) {
        return Future.succeededFuture(this.valueWrapperAtomicReference.get())
                .compose(pValueWrapper -> {
                    if (pValueWrapper == null) {
                        throw new ValueInvalidNow();
                    }
                    if (!pValueWrapper.isAliveNow()) {
                        throw new ValueInvalidNow();
                    }
                    return Future.succeededFuture(pValueWrapper.getValue());
                })
                .recover(throwable -> {
                    return loader.get()
                            .compose(p -> {
                                if (p == null) {
                                    this.set(p);
                                }
                                return Future.succeededFuture(p);
                            });
                });
    }

}
