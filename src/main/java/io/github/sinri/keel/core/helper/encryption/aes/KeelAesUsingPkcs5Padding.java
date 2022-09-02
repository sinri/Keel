package io.github.sinri.keel.core.helper.encryption.aes;

/**
 * @since 2.8
 */
abstract public class KeelAesUsingPkcs5Padding extends KeelAesBase {
    /**
     * @param key AES要求密钥长度为128位或192位或256位，java默认限制AES密钥长度最多128位
     */
    public KeelAesUsingPkcs5Padding(String key) {
        super(key);
    }
}
