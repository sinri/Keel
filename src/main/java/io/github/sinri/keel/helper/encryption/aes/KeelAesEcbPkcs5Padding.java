package io.github.sinri.keel.helper.encryption.aes;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

/**
 * @since 2.8
 */
public class KeelAesEcbPkcs5Padding extends KeelAesUsingPkcs5Padding {

    /**
     * @param key AES要求密钥长度为128位或192位或256位，java默认限制AES密钥长度最多128位
     */
    public KeelAesEcbPkcs5Padding(String key) {
        super(key);
    }

    public static void main(String[] args) throws Exception {
        KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
        SecretKey secretKey = keyGenerator.generateKey();
        String key = Base64.getEncoder().encodeToString(secretKey.getEncoded());
        System.out.println("generated key = " + key);

        KeelAesEcbPkcs5Padding aes = new KeelAesEcbPkcs5Padding(key);

        // 加密
        long lStart = System.currentTimeMillis();
        String enString = aes.encrypt("abcd中文测试加标点符号！@#￥%……&*（+——）（*&~，。，；,,/;lkk;ki;'[p]./,'\\467646789");
        System.out.println("加密后的字串是：" + enString);

        long lUseTime = System.currentTimeMillis() - lStart;
        System.out.println("加密耗时：" + lUseTime + "毫秒");

        // 解密
        lStart = System.currentTimeMillis();
        String DeString = aes.decrypt(enString);
        System.out.println("解密后的字串是：" + DeString);
        lUseTime = System.currentTimeMillis() - lStart;
        System.out.println("解密耗时：" + lUseTime + "毫秒");
    }

    @Override
    public SupportedCipherAlgorithm getCipherAlgorithm() {
        return SupportedCipherAlgorithm.AesEcbPkcs5Padding;
    }

    @Override
    public String encrypt(String source) {
        try {
            Cipher encryptionCipher = Cipher.getInstance(getCipherAlgorithm().getExpression());
            byte[] keyBytes = getKey().getBytes(ENCODING);
            encryptionCipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(keyBytes, KEY_ALGORITHM));
            byte[] encryptedMessageBytes = encryptionCipher.doFinal(source.getBytes(ENCODING));
            System.out.println("encrypt output bytes: " + encryptedMessageBytes.length);
            return Base64.getEncoder().encodeToString(encryptedMessageBytes);
        } catch (NoSuchPaddingException | IllegalBlockSizeException | NoSuchAlgorithmException | BadPaddingException |
                 InvalidKeyException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String decrypt(String encryptStr) {
        try {
            byte[] encryptedBase64 = Base64.getDecoder().decode(encryptStr);
            //System.out.println("decrypt input bytes: " + encryptedBase64.length);
            Cipher decryptionCipher = Cipher.getInstance(getCipherAlgorithm().getExpression());
            byte[] keyBytes = getKey().getBytes(ENCODING);
            decryptionCipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(keyBytes, KEY_ALGORITHM));
            byte[] decryptedMessageBytes = decryptionCipher.doFinal(encryptedBase64);
            return new String(decryptedMessageBytes, ENCODING);
        } catch (NoSuchPaddingException | IllegalBlockSizeException | NoSuchAlgorithmException | BadPaddingException |
                 InvalidKeyException e) {
            throw new RuntimeException(e);
        }
    }
}
