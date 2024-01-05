package io.github.sinri.keel.helper;

import io.vertx.ext.auth.VertxContextPRNG;

import static io.github.sinri.keel.facade.KeelInstance.keel;

/**
 * @since 3.0.1
 */
public class KeelRandomHelper {
    private static final KeelRandomHelper instance = new KeelRandomHelper();
    private final VertxContextPRNG prng;

    private KeelRandomHelper() {
        this.prng = VertxContextPRNG.current(keel.getVertx());
    }

    static KeelRandomHelper getInstance() {
        return instance;
    }

    /**
     * @return Pseudo Random Number Generator
     */
    public VertxContextPRNG getPRNG() {
        return prng;
    }
}
