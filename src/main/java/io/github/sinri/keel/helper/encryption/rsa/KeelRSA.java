package io.github.sinri.keel.helper.encryption.rsa;

import io.github.sinri.keel.helper.KeelHelpers;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Objects;

/**
 * @since 3.0.1
 */
public class KeelRSA extends KeelRSAKeyPair {
    public static final String SIGN_ALGORITHMS = "SHA1WithRSA";

    public static void main(String[] args) {
        try {
            String path = "/Users/leqee/code/Keel/log/rsa";
            KeelRSAKeyPair.generateKeyPairToDir(path);
            KeelRSA rsa = new KeelRSA();
            rsa.loadPublicKeyByKeyStoreFile(path + "/publicKey.keystore");
            rsa.loadPrivateKeyByKeyStoreFile(path + "/privateKey.keystore");

            byte[] encryptedByPrivateKey = rsa.encryptWithPrivateKey("HelloMe!".getBytes());
            byte[] decryptedByPublicKey = rsa.decryptWithPublicKey(encryptedByPrivateKey);
            System.out.println("encryptedByPrivateKey and decryptedByPublicKey: " + new String(decryptedByPublicKey));

            byte[] encryptedByPublicKey = rsa.encryptWithPublicKey("HelloMe!".getBytes());
            byte[] decryptedByPrivateKey = rsa.decryptWithPrivateKey(encryptedByPublicKey);
            System.out.println("encryptedByPublicKey and decryptedByPrivateKey: " + new String(decryptedByPrivateKey));

            String content = "GodJudgesAll";
            String sign = rsa.signWithPrivateKey(content.getBytes());
            System.out.println("sign: " + sign);
            boolean verified = rsa.verifySignWithPublicKey(content.getBytes(), sign);
            System.out.println("verified: " + verified);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    /**
     * @param plainTextData data to encrypt
     * @return encrypted data
     * @throws NoSuchPaddingException
     * @throws NoSuchAlgorithmException  无此加密算法
     * @throws InvalidKeyException       加密公钥非法
     * @throws IllegalBlockSizeException 明文长度非法
     * @throws BadPaddingException       明文数据已损坏
     */
    public byte[] encryptWithPublicKey(byte[] plainTextData) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        // 使用默认RSA
        Cipher cipher = Cipher.getInstance("RSA");
        // cipher= Cipher.getInstance("RSA", new BouncyCastleProvider());
        cipher.init(Cipher.ENCRYPT_MODE, Objects.requireNonNull(getPublicKey()));
        return cipher.doFinal(plainTextData);
    }

    /**
     * @param plainTextData data to encrypt
     * @return encrypted data
     * @throws NoSuchPaddingException
     * @throws NoSuchAlgorithmException  无此加密算法
     * @throws InvalidKeyException       加密私钥非法
     * @throws IllegalBlockSizeException 明文长度非法
     * @throws BadPaddingException       明文数据已损坏
     */
    public byte[] encryptWithPrivateKey(byte[] plainTextData) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, Objects.requireNonNull(getPrivateKey()));
        return cipher.doFinal(plainTextData);
    }

    /**
     * @param cipherData encrypted data
     * @return decrypted data
     * @throws NoSuchPaddingException
     * @throws NoSuchAlgorithmException  无此解密算法
     * @throws InvalidKeyException       解密私钥非法
     * @throws IllegalBlockSizeException 密文长度非法
     * @throws BadPaddingException       密文数据已损坏
     */
    public byte[] decryptWithPrivateKey(byte[] cipherData) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        // 使用默认RSA
        Cipher cipher = Cipher.getInstance("RSA");
        // cipher= Cipher.getInstance("RSA", new BouncyCastleProvider());
        cipher.init(Cipher.DECRYPT_MODE, Objects.requireNonNull(getPrivateKey()));
        return cipher.doFinal(cipherData);
    }

    /**
     * @param cipherData encrypted data
     * @return decrypted data
     * @throws NoSuchPaddingException
     * @throws NoSuchAlgorithmException  无此解密算法
     * @throws InvalidKeyException       解密公钥非法
     * @throws IllegalBlockSizeException 密文长度非法
     * @throws BadPaddingException       密文数据已损坏
     */
    public byte[] decryptWithPublicKey(byte[] cipherData) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        // 使用默认RSA
        Cipher cipher = Cipher.getInstance("RSA");
        // cipher= Cipher.getInstance("RSA", new BouncyCastleProvider());
        cipher.init(Cipher.DECRYPT_MODE, Objects.requireNonNull(getPublicKey()));
        return cipher.doFinal(cipherData);
    }

    /**
     * 利用私钥进行RSA签名.
     *
     * @param content 待签名的数据块
     * @return RSA签名结果
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeySpecException
     * @throws InvalidKeyException
     * @throws SignatureException
     */
    public String signWithPrivateKey(byte[] content) throws NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException, SignatureException {
        byte[] bytesOfPrivateKey = this.getPrivateKey().getEncoded();
        PKCS8EncodedKeySpec priPKCS8 = new PKCS8EncodedKeySpec(bytesOfPrivateKey);
        KeyFactory keyf = KeyFactory.getInstance("RSA");
        PrivateKey priKey = keyf.generatePrivate(priPKCS8);
        java.security.Signature signature = java.security.Signature.getInstance(SIGN_ALGORITHMS);
        signature.initSign(priKey);
        signature.update(content);
        byte[] signed = signature.sign();
        return KeelHelpers.binaryHelper().encodeWithBase64ToString(signed);
    }

    /**
     * 利用公钥进行RSA签名校验。
     *
     * @param content 被签名的数据块
     * @param sign    RSA签名
     * @return 校验结果
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeySpecException
     * @throws InvalidKeyException
     * @throws SignatureException
     */
    public boolean verifySignWithPublicKey(byte[] content, String sign) throws NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException, SignatureException {
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        byte[] encodedKey = getPublicKey().getEncoded();
        PublicKey pubKey = keyFactory.generatePublic(new X509EncodedKeySpec(encodedKey));

        java.security.Signature signature = java.security.Signature.getInstance(SIGN_ALGORITHMS);
        signature.initVerify(pubKey);
        signature.update(content);

        return signature.verify(KeelHelpers.stringHelper().decodeWithBase64ToBytes(sign));
    }
}
