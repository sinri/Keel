package io.github.sinri.keel.core.helper.encryption;

import io.github.sinri.keel.core.helper.encryption.aes.KeelAes;

/**
 * @since 2.8
 */
public class KeelCryptographyHelper {
    private static final KeelCryptographyHelper instance = new KeelCryptographyHelper();

    private KeelCryptographyHelper() {

    }

    public static KeelCryptographyHelper getInstance() {
        return instance;
    }

    public KeelAes aes(KeelAes.SupportedCipherAlgorithm cipherAlgorithm, String key) {
        return KeelAes.create(cipherAlgorithm, key);
    }
}
