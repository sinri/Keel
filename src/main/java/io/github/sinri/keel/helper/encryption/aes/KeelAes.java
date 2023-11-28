package io.github.sinri.keel.helper.encryption.aes;

import javax.annotation.Nonnull;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Objects;

/**
 * @since 2.8
 */
public interface KeelAes {
    /**
     * 密钥算法
     */
    String KEY_ALGORITHM = "AES";
    /**
     * 编码格式
     */
    Charset ENCODING = StandardCharsets.UTF_8;

    static @Nonnull KeelAes create(@Nonnull SupportedCipherAlgorithm cipherAlgorithm, @Nonnull String key) {
        Objects.requireNonNull(cipherAlgorithm);
        Objects.requireNonNull(key);
        switch (cipherAlgorithm) {
            case AesCbcPkcs7Padding:
                return new KeelAesCbcPkcs7Padding(key);
            case AesCbcPkcs5Padding:
                return new KeelAesCbcPkcs5Padding(key);
            case AesEcbPkcs5Padding:
                return new KeelAesEcbPkcs5Padding(key);
            case AesEcbPkcs7Padding:
                return new KeelAesEcbPkcs7Padding(key);
            case AesEcbNoPadding:
                return new KeelAesEcbNoPadding(key);
        }
        throw new IllegalArgumentException();
    }

    /**
     * 加密/解密算法 / 工作模式 / 填充方式
     * Java 6支持PKCS5Padding填充方式
     * Bouncy Castle支持PKCS7Padding填充方式
     */
    SupportedCipherAlgorithm getCipherAlgorithm();

    String encrypt(String source);

    String decrypt(String encryptStr);

    /**
     * @since 3.0.11 be static.
     */
    static SecretKey generateSecretKeyWithKeySize(int keySize) throws NoSuchAlgorithmException {
        KeyGenerator keyGenerator = KeyGenerator.getInstance(KEY_ALGORITHM);
        //keyGenerator.init(256);
        keyGenerator.init(keySize);
        return keyGenerator.generateKey();
    }

    /**
     * @since 3.0.11 be static.
     */
    static String generate128BitsSecretKey() throws NoSuchAlgorithmException {
        return Base64.getEncoder().encodeToString(generateSecretKeyWithKeySize(128).getEncoded());
    }

    /**
     * @since 3.0.11 be static.
     */
    static String generate192BitsSecretKey() throws NoSuchAlgorithmException {
        return Base64.getEncoder().encodeToString(generateSecretKeyWithKeySize(192).getEncoded());
    }

    /**
     * @since 3.0.11 be static.
     */
    static String generate256BitsSecretKey() throws NoSuchAlgorithmException {
        return Base64.getEncoder().encodeToString(generateSecretKeyWithKeySize(256).getEncoded());
    }

    enum SupportedCipherAlgorithm {
        AesCbcPkcs5Padding("AES/CBC/PKCS5Padding"),
        AesCbcPkcs7Padding("AES/CBC/PKCS7Padding"),
        AesEcbPkcs5Padding("AES/ECB/PKCS5Padding"),
        AesEcbPkcs7Padding("AES/ECB/PKCS7Padding"),
        AesEcbNoPadding("AES/ECB/NoPadding"),
        ;

        private final String expression;

        SupportedCipherAlgorithm(String expression) {
            this.expression = expression;
        }

        public String getExpression() {
            return expression;
        }
    }
}
