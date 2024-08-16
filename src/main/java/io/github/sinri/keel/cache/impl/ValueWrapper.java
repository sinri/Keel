package io.github.sinri.keel.cache.impl;

import javax.annotation.Nullable;
import java.lang.ref.SoftReference;
import java.util.Date;

/**
 * @since 2.5 moved from inner class to here
 * @since 3.2.15 value use SoftReference
 */
public class ValueWrapper<P> {
    private final SoftReference<P> value;
    private final long death;
    private final long birth;

    public ValueWrapper(P value, long lifeInSeconds) {
        this.value = new SoftReference<>(value);
        this.birth = System.currentTimeMillis();//new Date().getTime();
        this.death = this.birth + lifeInSeconds * 1000L;
    }

    public long getBirth() {
        return birth;
    }

    public long getDeath() {
        return death;
    }

    @Nullable
    public P getValue() {
        return value.get();
    }

    public boolean isAliveNow() {
        return value.get() != null && (new Date().getTime()) < this.death;
    }
}
