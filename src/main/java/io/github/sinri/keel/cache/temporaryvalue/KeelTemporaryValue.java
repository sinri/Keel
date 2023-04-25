package io.github.sinri.keel.cache.temporaryvalue;

import io.github.sinri.keel.cache.ValueWrapper;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

/**
 * @since 3.0.1
 */
public class KeelTemporaryValue<P> {
    private final AtomicReference<ValueWrapper<P>> valueWrapperAtomicReference;
    private long defaultLifetimeInSeconds = 10L;

    public KeelTemporaryValue() {
        this.valueWrapperAtomicReference = new AtomicReference<>();
    }

    public KeelTemporaryValue<P> setDefaultLifetimeInSeconds(long defaultLifetimeInSeconds) {
        this.defaultLifetimeInSeconds = defaultLifetimeInSeconds;
        return this;
    }

    public KeelTemporaryValue<P> set(P p) {
        return set(p, defaultLifetimeInSeconds);
    }

    public KeelTemporaryValue<P> set(P p, long lifeInSeconds) {
        this.valueWrapperAtomicReference.set(new ValueWrapper<>(p, lifeInSeconds));
        return this;
    }

    public P rawGet() throws ValueInvalidNow {
        ValueWrapper<P> pValueWrapper = this.valueWrapperAtomicReference.get();
        if (pValueWrapper == null) {
            throw new ValueInvalidNow();
        }
        if (!pValueWrapper.isAliveNow()) {
            throw new ValueInvalidNow();
        }
        return pValueWrapper.getValue();
    }

    public P get() {
        return getOrElse(null);
    }

    public P getOrElse(P fallback) {
        try {
            return rawGet();
        } catch (ValueInvalidNow e) {
            return fallback;
        }
    }

    public P getOrReload(@NotNull Supplier<P> loader) {
        ValueWrapper<P> pValueWrapper = this.valueWrapperAtomicReference.get();
        try {
            if (pValueWrapper == null) {
                throw new ValueInvalidNow();
            }
            if (!pValueWrapper.isAliveNow()) {
                throw new ValueInvalidNow();
            }
            return pValueWrapper.getValue();
        } catch (ValueInvalidNow e) {
            var p = loader.get();
            if (p != null) {
                this.set(p);
            }
            return p;
        }
    }


}