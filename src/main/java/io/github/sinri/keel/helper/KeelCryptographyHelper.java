package io.github.sinri.keel.helper;

import io.github.sinri.keel.helper.encryption.aes.KeelAes;

/**
 * @since 2.8
 */
public class KeelCryptographyHelper {
    private static final KeelCryptographyHelper instance = new KeelCryptographyHelper();

    private KeelCryptographyHelper() {

    }

    static KeelCryptographyHelper getInstance() {
        return instance;
    }

    public KeelAes aes(KeelAes.SupportedCipherAlgorithm cipherAlgorithm, String key) {
        return KeelAes.create(cipherAlgorithm, key);
    }
}
