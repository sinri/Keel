package io.github.sinri.keel.core.helper.encryption.aes;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.Base64;

/**
 * @since 2.8
 */
public class KeelAesEcbPkcs7Padding extends KeelAesUsingPkcs7Padding {

    /**
     * @param key AES要求密钥长度为128位或192位或256位，java默认限制AES密钥长度最多128位
     */
    public KeelAesEcbPkcs7Padding(String key) {
        super(key);
    }

    public static void main(String[] args) throws Exception {
        String key = "1234567890123456";// + "X";
        KeelAesEcbPkcs7Padding aes = new KeelAesEcbPkcs7Padding(key);

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
        return SupportedCipherAlgorithm.AesEcbPkcs7Padding;
    }

    /**
     * AES加密
     *
     * @param source 源字符串
     * @return 加密后的密文
     */
    public String encrypt(String source) {
        try {
            byte[] sourceBytes = source.getBytes(ENCODING);
            byte[] keyBytes = getKey().getBytes(ENCODING);
            Cipher cipher = Cipher.getInstance(getCipherAlgorithm().getExpression(), "BC");
            SecretKeySpec secretKeySpec = new SecretKeySpec(keyBytes, "AES");
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);
            byte[] decrypted = cipher.doFinal(sourceBytes);
            return Base64.getEncoder().encodeToString(decrypted);
        } catch (UnsupportedEncodingException | NoSuchPaddingException | IllegalBlockSizeException |
                 NoSuchAlgorithmException | BadPaddingException | NoSuchProviderException |
                 InvalidKeyException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * AES解密
     *
     * @param encryptStr 加密后的密文
     * @return 源字符串
     */
    public String decrypt(String encryptStr) {
        try {
            byte[] sourceBytes = Base64.getDecoder().decode(encryptStr);
            byte[] keyBytes = getKey().getBytes(ENCODING);
            Cipher cipher = Cipher.getInstance(getCipherAlgorithm().getExpression(), "BC");
            SecretKeySpec secretKey = new SecretKeySpec(keyBytes, "AES");
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            byte[] decoded = cipher.doFinal(sourceBytes);
            return new String(decoded, ENCODING);
        } catch (UnsupportedEncodingException | NoSuchPaddingException | IllegalBlockSizeException |
                 NoSuchAlgorithmException | BadPaddingException | NoSuchProviderException |
                 InvalidKeyException e) {
            throw new RuntimeException(e);
        }
    }
}
