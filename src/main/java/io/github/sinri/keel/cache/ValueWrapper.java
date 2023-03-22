package io.github.sinri.keel.cache;

import java.util.Date;

/**
 * @since 2.5 moved from inner class to here
 */
public class ValueWrapper<P> {
    private final P value;
    private final long death;
    private final long birth;

    public ValueWrapper(P value, long lifeInSeconds) {
        this.value = value;
        this.birth = new Date().getTime();
        this.death = this.birth + lifeInSeconds * 1000L;
    }

    public long getBirth() {
        return birth;
    }

    public long getDeath() {
        return death;
    }

    public P getValue() {
        return value;
    }

    public boolean isAliveNow() {
        return  new Date().getTime() < this.death;
    }
}
