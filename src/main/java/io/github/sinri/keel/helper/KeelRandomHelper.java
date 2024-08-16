package io.github.sinri.keel.helper;

import java.security.SecureRandom;
import java.util.Random;

/**
 * @since 3.0.1
 */
public class KeelRandomHelper {
    private static final KeelRandomHelper instance = new KeelRandomHelper();

    private final Random random = new Random();
    private final SecureRandom secureRandom = new SecureRandom();

    private KeelRandomHelper() {
    }

    static KeelRandomHelper getInstance() {
        return instance;
    }


    /**
     * @since 4.0.0
     */
    public Random getRandom() {
        return random;
    }

    /**
     * @since 4.0.0
     */
    public SecureRandom getSecureRandom() {
        return secureRandom;
    }
}
