package io.github.sinri.keel.helper.encryption.rsa;

import io.github.sinri.keel.helper.KeelHelpers;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.security.*;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

/**
 * @since 3.0.1
 */
public class KeelRSAKeyPair {
    private RSAPublicKey publicKey;
    private RSAPrivateKey privateKey;

    public KeelRSAKeyPair() {
        this.privateKey = null;
        this.publicKey = null;
    }

    /**
     * 随机生成密钥对
     */
    public static void generateKeyPairToDir(String dirPath) throws NoSuchAlgorithmException {
        // KeyPairGenerator类用于生成公钥和私钥对，基于RSA算法生成对象
        KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance("RSA");
        // 初始化密钥对生成器，密钥大小为96-1024位
        keyPairGen.initialize(1024, new SecureRandom());
        // 生成一个密钥对，保存在keyPair中
        KeyPair keyPair = keyPairGen.generateKeyPair();
        // 得到私钥
        RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();
        // 得到公钥
        RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();

        // 得到公钥字符串
        String publicKeyString = KeelHelpers.binaryHelper().encodeWithBase64ToString(publicKey.getEncoded());//Base64.encode(publicKey.getEncoded());
        String publicKeyFilePath = dirPath + "/publicKey.keystore";
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(publicKeyFilePath))) {
            bw.write(publicKeyString);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        // 得到私钥字符串
        String privateKeyString = KeelHelpers.binaryHelper().encodeWithBase64ToString(privateKey.getEncoded());//Base64.encode(privateKey.getEncoded());
        String privateKeyFilePath = dirPath + "/privateKey.keystore";
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(privateKeyFilePath))) {
            bw.write(privateKeyString);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 从私钥文件中加载私钥实例.
     *
     * @param path 私钥文件路径 such as `privateKey.keystore`
     * @throws IOException              读取对应文件出错
     * @throws NoSuchAlgorithmException 没有这种算法
     * @throws InvalidKeySpecException  不可用的公钥
     */
    public KeelRSAKeyPair loadPrivateKeyByKeyStoreFile(String path) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        byte[] bytes = KeelHelpers.fileHelper().readFileAsByteArray(path, true);
        return loadPrivateKeyByStr(new String(bytes));
    }

    /**
     * 从私钥文件内容中加载私钥实例.
     *
     * @param privateKeyStr content of private key in keystore format
     * @throws NoSuchAlgorithmException 没有这种算法
     * @throws InvalidKeySpecException  不可用的公钥
     */
    public KeelRSAKeyPair loadPrivateKeyByStr(String privateKeyStr) throws NoSuchAlgorithmException, InvalidKeySpecException {
        byte[] buffer = KeelHelpers.stringHelper().decodeWithBase64ToBytes(privateKeyStr);//Base64.decode(privateKeyStr);

        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(buffer);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        this.privateKey = (RSAPrivateKey) keyFactory.generatePrivate(keySpec);
        return this;
    }

    /**
     * 从公钥文件中加载公钥实例.
     *
     * @param path 公钥文件路径 such as `publicKey.keystore`
     * @throws IOException              读取对应文件出错
     * @throws NoSuchAlgorithmException 没有这种算法
     * @throws InvalidKeySpecException  不可用的公钥
     */
    public KeelRSAKeyPair loadPublicKeyByKeyStoreFile(String path) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        byte[] bytes = KeelHelpers.fileHelper().readFileAsByteArray(path, true);
        return loadPublicKeyByStr(new String(bytes));
    }

    /**
     * 从公钥文件内容中加载公钥实例.
     *
     * @param publicKeyStr 公钥文件内容
     * @throws NoSuchAlgorithmException 没有这种算法
     * @throws InvalidKeySpecException  不可用的公钥
     */
    public KeelRSAKeyPair loadPublicKeyByStr(String publicKeyStr) throws NoSuchAlgorithmException, InvalidKeySpecException {
        byte[] buffer = KeelHelpers.stringHelper().decodeWithBase64ToBytes(publicKeyStr);//Base64.decode(publicKeyStr);

        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(buffer);
        this.publicKey = (RSAPublicKey) keyFactory.generatePublic(keySpec);
        return this;
    }

    public RSAPrivateKey getPrivateKey() {
        return privateKey;
    }

    public KeelRSAKeyPair setPrivateKey(RSAPrivateKey privateKey) {
        this.privateKey = privateKey;
        return this;
    }

    public RSAPublicKey getPublicKey() {
        return publicKey;
    }

    public KeelRSAKeyPair setPublicKey(RSAPublicKey publicKey) {
        this.publicKey = publicKey;
        return this;
    }
}
