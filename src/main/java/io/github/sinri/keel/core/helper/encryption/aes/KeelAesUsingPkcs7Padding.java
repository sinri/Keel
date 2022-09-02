package io.github.sinri.keel.core.helper.encryption.aes;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.security.Security;

/**
 * @since 2.8
 */
abstract public class KeelAesUsingPkcs7Padding extends KeelAesBase {
    static {
        //如果是PKCS7Padding填充方式，则必须加上下面这行
        Security.addProvider(new BouncyCastleProvider());
    }

    /**
     * @param key AES要求密钥长度为128位或192位或256位，java默认限制AES密钥长度最多128位
     */
    public KeelAesUsingPkcs7Padding(String key) {
        super(key);
    }
}
