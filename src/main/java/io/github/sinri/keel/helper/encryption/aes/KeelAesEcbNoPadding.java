package io.github.sinri.keel.helper.encryption.aes;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import static io.github.sinri.keel.helper.KeelHelpersInterface.KeelHelpers;

/**
 * @since 3.0.11
 */
public class KeelAesEcbNoPadding extends KeelAesBase {
    /**
     * @param key AES要求密钥长度为128位或192位或256位，java默认限制AES密钥长度最多128位
     */
    public KeelAesEcbNoPadding(String key) {
        super(key);
    }

    public static void main(String[] args) throws NoSuchAlgorithmException {
        String key = KeelAes.generate128BitsSecretKey();
        KeelAesEcbNoPadding aes = new KeelAesEcbNoPadding(key);
        String source = "1234123409870987";
        String encrypted = aes.encrypt(source);
        System.out.println(encrypted);
        String decrypted = aes.decrypt(encrypted);
        System.out.println(decrypted);
    }

    @Override
    public SupportedCipherAlgorithm getCipherAlgorithm() {
        return SupportedCipherAlgorithm.AesEcbNoPadding;
    }

    /**
     * @param source AES的NoPadding模式加密的key和data的byte字节数必须为16的倍数。
     */
    @Override
    public String encrypt(String source) {
        try {
//            System.out.println("source: " + source);
            SecretKeySpec secretKeySpec = new SecretKeySpec(getKey().getBytes(ENCODING), KEY_ALGORITHM);
            Cipher cipher = Cipher.getInstance(getCipherAlgorithm().getExpression());
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);
            byte[] sourceBytes = source.getBytes(ENCODING);
//            System.out.println("source bytes: " + KeelHelpers.binaryHelper().encodeHexWithUpperDigits(sourceBytes));
            byte[] encryptedBytes = cipher.doFinal(sourceBytes);
//            System.out.println("encrypted bytes: " + KeelHelpers.binaryHelper().encodeHexWithUpperDigits(encryptedBytes));
            return KeelHelpers.binaryHelper().encodeWithBase64ToString(encryptedBytes);
        } catch (NoSuchPaddingException | IllegalBlockSizeException | NoSuchAlgorithmException | BadPaddingException |
                 InvalidKeyException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String decrypt(String encryptStr) {
        try {
            SecretKeySpec secretKeySpec = new SecretKeySpec(getKey().getBytes(ENCODING), KEY_ALGORITHM);
            Cipher cipher = Cipher.getInstance(getCipherAlgorithm().getExpression());
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec);
            byte[] encryptedBytes = KeelHelpers.stringHelper().decodeWithBase64ToBytes(encryptStr);
//            System.out.println("encrypted bytes: " + KeelHelpers.binaryHelper().encodeHexWithUpperDigits(encryptedBytes));
            var decryptedBytes = cipher.doFinal(encryptedBytes);
//            System.out.println("decrypted bytes: " + KeelHelpers.binaryHelper().encodeHexWithUpperDigits(decryptedBytes));
            var s = new String(decryptedBytes, ENCODING);
//            System.out.println("decrypted text: " + s);
            return s;
        } catch (NoSuchPaddingException | IllegalBlockSizeException | NoSuchAlgorithmException | BadPaddingException |
                 InvalidKeyException e) {
            throw new RuntimeException(e);
        }
    }
}
