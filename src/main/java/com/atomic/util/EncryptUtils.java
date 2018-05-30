package com.atomic.util;

import com.atomic.enums.AlgorithmEnum;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

/**
 * @author dreamyao
 * @version 1.0.0
 * @title 通用加密解密工具类
 * @Data 2017/8/13 16:37
 */
public final class EncryptUtils {

    private static final String CHARSET_UTF8 = "UTF-8";

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    private EncryptUtils() {
    }

    /*============================================================================
                                  单向加密 MD5,SHA
    ============================================================================*/

    /**
     * 单向加密
     * @param algorithm MD2,MD5,SHA-1,SHA-256,SHA-384,SHA-512
     * @param value     需要加密的值
     * @return
     * @throws NoSuchAlgorithmException
     */
    public static byte[] digest(String algorithm, String value) throws NoSuchAlgorithmException {
        if (StringUtils.isBlank(algorithm)) {
            throw new NullPointerException("algorithm is null.");
        }
        if (StringUtils.isBlank(value)) {
            throw new NullPointerException("digest value is null.");
        }
        MessageDigest digest = MessageDigest.getInstance(algorithm);
        digest.update(value.getBytes());
        return digest.digest();
    }

    /**
     * 单向加密并转换为字符串格式
     * @param algorithm MD2,MD5,SHA-1,SHA-256,SHA-384,SHA-512
     * @param value     需要加密的值
     * @return
     * @throws NoSuchAlgorithmException
     */
    public static String digestToString(String algorithm, String value) throws NoSuchAlgorithmException {
        byte[] bytes = digest(algorithm, value);
        return Base64.encodeBase64String(bytes);
    }

    /**
     * md5 加密
     * @param value
     * @return
     * @throws NoSuchAlgorithmException
     */
    public static byte[] md5(String value) throws NoSuchAlgorithmException {
        return digest(AlgorithmEnum.MD5.value(), value);
    }

    /**
     * md5加密并转换为字符串格式
     * @param value
     * @return
     * @throws NoSuchAlgorithmException
     */
    public static String md5ToString(String value) throws NoSuchAlgorithmException {
        return digestToString(AlgorithmEnum.MD5.value(), value);
    }

    /**
     * sha加密
     * @param value
     * @return
     * @throws NoSuchAlgorithmException
     */
    public static byte[] sha(String value) throws NoSuchAlgorithmException {
        return digest(AlgorithmEnum.SHA.value(), value);
    }

    /**
     * sha加密并转换为字符串格式
     * @param value
     * @return
     * @throws NoSuchAlgorithmException
     */
    public static String shaToString(String value) throws NoSuchAlgorithmException {
        return digestToString(AlgorithmEnum.SHA.value(), value);
    }


    /*===============================================================================
                                     对称双向加密AES,DES
    ===============================================================================*/

    /**
     * 生成密钥
     * @param algorithm 请求密钥算法的名称
     * @return
     * @throws NoSuchAlgorithmException
     */
    public static SecretKey generateSecretKey(String algorithm) throws NoSuchAlgorithmException {
        if (StringUtils.isBlank(algorithm)) {
            throw new NullPointerException("algorithm name is null");
        }
        KeyGenerator keyGenerator = KeyGenerator.getInstance(algorithm);
        return keyGenerator.generateKey();
    }

    /**
     * 生成密钥字符串
     * @param algorithm 请求密钥算法的名称
     * @return
     * @throws NoSuchAlgorithmException
     */
    public static String generateSecretKeyString(String algorithm) throws NoSuchAlgorithmException {
        SecretKey secretKey = generateSecretKey(algorithm);
        return Base64.encodeBase64String(secretKey.getEncoded());
    }

    /**
     * 生成AES密钥
     * @return
     * @throws NoSuchAlgorithmException
     */
    public static SecretKey aesKey() throws NoSuchAlgorithmException {
        return generateSecretKey(AlgorithmEnum.AES.value());
    }

    /**
     * 生成AES密钥字符串
     * @return
     * @throws NoSuchAlgorithmException
     */
    public static String aesKeyString() throws NoSuchAlgorithmException {
        return generateSecretKeyString(AlgorithmEnum.AES.value());
    }

    /**
     * 生成DES密钥
     * @return
     * @throws NoSuchAlgorithmException
     */
    public static SecretKey desKey() throws NoSuchAlgorithmException {
        return generateSecretKey(AlgorithmEnum.DES.value());
    }

    /**
     * 生成DES密钥字符串
     * @return
     * @throws NoSuchAlgorithmException
     */
    public static String desKeyString() throws NoSuchAlgorithmException {
        return generateSecretKeyString(AlgorithmEnum.DES.value());
    }

    /**
     * 解析SecretKey
     * @param algorithm 请求密钥算法的名称
     * @param value
     * @return
     * @throws NoSuchAlgorithmException
     */
    public static SecretKey parseSecretKey(String algorithm, String value) throws NoSuchAlgorithmException {
        byte[] bytes = Base64.decodeBase64(value);
        return parseSecretKey(algorithm, bytes);
    }

    /**
     * 解析SecretKey
     * @param algorithm
     * @param bytes
     * @return
     */
    public static SecretKey parseSecretKey(String algorithm, byte[] bytes) {
        return new SecretKeySpec(bytes, 0, bytes.length, algorithm);
    }

    /**
     * 解析 AES SecretKey
     * @param value
     * @return
     * @throws NoSuchAlgorithmException
     */
    public static SecretKey parseAESSecretKey(String value) throws NoSuchAlgorithmException {
        return parseSecretKey(AlgorithmEnum.AES.value(), value);
    }

    /**
     * 解析 AES SecretKey
     * @param bytes
     * @return
     * @throws NoSuchAlgorithmException
     */
    public static SecretKey parseAESSecretKey(byte[] bytes) throws NoSuchAlgorithmException {
        return parseSecretKey(AlgorithmEnum.AES.value(), bytes);
    }

    /**
     * 解析 DES SecretKey
     * @param value
     * @return
     * @throws NoSuchAlgorithmException
     */
    public static SecretKey parseDESSecretKey(String value) throws NoSuchAlgorithmException {
        return parseSecretKey(AlgorithmEnum.DES.value(), value);
    }

    /**
     * 解析 DES SecretKey
     * @param bytes
     * @return
     * @throws NoSuchAlgorithmException
     */
    public static SecretKey parseDESSecretKey(byte[] bytes) throws NoSuchAlgorithmException {
        return parseSecretKey(AlgorithmEnum.DES.value(), bytes);
    }

    /**
     * 对称加密
     * @param algorithm 请求密钥算法的名称
     * @param key
     * @param value
     * @return
     * @throws NoSuchAlgorithmException
     * @throws NoSuchPaddingException
     * @throws InvalidKeyException
     * @throws IllegalBlockSizeException
     * @throws BadPaddingException
     */
    public static byte[] encryptSymmetric(String algorithm, SecretKey key, String value) throws NoSuchAlgorithmException,
            NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        if (StringUtils.isBlank(algorithm)) {
            throw new NullPointerException("algorithm name is null");
        }
        if (StringUtils.isBlank(value)) {
            throw new NullPointerException("encrypt value is null");
        }
        Cipher cipher = Cipher.getInstance(algorithm);
        cipher.init(Cipher.ENCRYPT_MODE, key);
        return cipher.doFinal(value.getBytes());
    }

    /**
     * 对称加密
     * @param algorithm 请求密钥算法的名称
     * @param key
     * @param value
     * @return
     * @throws NoSuchAlgorithmException
     * @throws NoSuchPaddingException
     * @throws InvalidKeyException
     * @throws IllegalBlockSizeException
     * @throws BadPaddingException
     */
    public static String encryptSymmetricString(String algorithm, SecretKey key, String value) throws NoSuchAlgorithmException,
            NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        byte[] bytes = encryptSymmetric(algorithm, key, value);
        return Base64.encodeBase64String(bytes);
    }

    /**
     * 对称解密
     * @param algorithm 请求密钥算法的名称
     * @param key
     * @param value
     * @return
     * @throws NoSuchAlgorithmException
     * @throws NoSuchPaddingException
     * @throws InvalidKeyException
     * @throws IllegalBlockSizeException
     * @throws BadPaddingException
     * @throws UnsupportedEncodingException
     */
    public static byte[] decryptSymmetric(String algorithm, SecretKey key, String value) throws NoSuchAlgorithmException,
            NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, UnsupportedEncodingException {
        if (StringUtils.isBlank(algorithm)) {
            throw new NullPointerException("algorithm name is null");
        }
        if (StringUtils.isBlank(value)) {
            throw new NullPointerException("decrypt value is null");
        }
        Cipher cipher = Cipher.getInstance(algorithm);
        cipher.init(Cipher.DECRYPT_MODE, key);
        byte[] decodeBytes = Base64.decodeBase64(value);
        return cipher.doFinal(decodeBytes);
    }

    /**
     * 对称解密
     * @param algorithm
     * @param key
     * @param value
     * @return
     * @throws NoSuchAlgorithmException
     * @throws NoSuchPaddingException
     * @throws InvalidKeyException
     * @throws IllegalBlockSizeException
     * @throws BadPaddingException
     * @throws UnsupportedEncodingException
     */
    public static String decryptSymmetricString(String algorithm, SecretKey key, String value) throws NoSuchAlgorithmException,
            NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, UnsupportedEncodingException {
        byte[] bytes = decryptSymmetric(algorithm, key, value);
        return new String(bytes, CHARSET_UTF8);
    }

    /**
     * DES对称加密
     * @param key
     * @param value
     * @return
     * @throws InvalidKeyException
     * @throws NoSuchAlgorithmException
     * @throws NoSuchPaddingException
     * @throws IllegalBlockSizeException
     * @throws BadPaddingException
     */
    public static byte[] desEncrypt(SecretKey key, String value) throws InvalidKeyException, NoSuchAlgorithmException,
            NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException {
        return encryptSymmetric(AlgorithmEnum.DES.value(), key, value);
    }

    /**
     * DES对称加密
     * @param key
     * @param value
     * @return
     * @throws InvalidKeyException
     * @throws NoSuchAlgorithmException
     * @throws NoSuchPaddingException
     * @throws IllegalBlockSizeException
     * @throws BadPaddingException
     */
    public static String desEncryptString(SecretKey key, String value) throws InvalidKeyException, NoSuchAlgorithmException,
            NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException {
        return encryptSymmetricString(AlgorithmEnum.DES.value(), key, value);
    }

    /**
     * DES对称解密
     * @param key
     * @param value
     * @return
     * @throws InvalidKeyException
     * @throws NoSuchAlgorithmException
     * @throws NoSuchPaddingException
     * @throws IllegalBlockSizeException
     * @throws BadPaddingException
     * @throws UnsupportedEncodingException
     */
    public static String desDecryptString(SecretKey key, String value) throws InvalidKeyException, NoSuchAlgorithmException,
            NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, UnsupportedEncodingException {
        return decryptSymmetricString(AlgorithmEnum.DES.value(), key, value);
    }

    /**
     * DES对称解密
     * @param key
     * @param value
     * @return
     * @throws InvalidKeyException
     * @throws NoSuchAlgorithmException
     * @throws NoSuchPaddingException
     * @throws IllegalBlockSizeException
     * @throws BadPaddingException
     * @throws UnsupportedEncodingException
     */
    public static byte[] desDecrypt(SecretKey key, String value) throws InvalidKeyException, NoSuchAlgorithmException,
            NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, UnsupportedEncodingException {
        return decryptSymmetric(AlgorithmEnum.DES.value(), key, value);
    }

    /**
     * AES对称加密
     * @param key
     * @param value
     * @return
     * @throws InvalidKeyException
     * @throws NoSuchAlgorithmException
     * @throws NoSuchPaddingException
     * @throws IllegalBlockSizeException
     * @throws BadPaddingException
     */
    public static byte[] aesEncrypt(SecretKey key, String value) throws InvalidKeyException, NoSuchAlgorithmException,
            NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException {
        return encryptSymmetric(AlgorithmEnum.AES.value(), key, value);
    }

    /**
     * AES对称加密
     * @param key
     * @param value
     * @return
     * @throws InvalidKeyException
     * @throws NoSuchAlgorithmException
     * @throws NoSuchPaddingException
     * @throws IllegalBlockSizeException
     * @throws BadPaddingException
     */
    public static String aesEncryptString(SecretKey key, String value) throws InvalidKeyException, NoSuchAlgorithmException,
            NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException {
        return encryptSymmetricString(AlgorithmEnum.AES.value(), key, value);
    }

    /**
     * AES对称解密
     * @param key
     * @param value
     * @return
     * @throws InvalidKeyException
     * @throws NoSuchAlgorithmException
     * @throws NoSuchPaddingException
     * @throws IllegalBlockSizeException
     * @throws BadPaddingException
     * @throws UnsupportedEncodingException
     */
    public static byte[] aesDecrypt(SecretKey key, String value) throws InvalidKeyException, NoSuchAlgorithmException,
            NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, UnsupportedEncodingException {
        return decryptSymmetric(AlgorithmEnum.AES.value(), key, value);
    }

    /**
     * AES对称解密
     * @param key
     * @param value
     * @return
     * @throws InvalidKeyException
     * @throws NoSuchAlgorithmException
     * @throws NoSuchPaddingException
     * @throws IllegalBlockSizeException
     * @throws BadPaddingException
     * @throws UnsupportedEncodingException
     */
    public static String aesDecryptString(SecretKey key, String value) throws InvalidKeyException, NoSuchAlgorithmException,
            NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, UnsupportedEncodingException {
        return decryptSymmetricString(AlgorithmEnum.AES.value(), key, value);
    }


    /*================================================================================
                                    非对称双向加密RSA/DSA
    =================================================================================*/

    /**
     * 生成密钥对
     * @param algorithm
     * @param keysize
     * @return
     * @throws NoSuchAlgorithmException
     */
    public static KeyPair generateKeyPair(String algorithm, int keysize) throws NoSuchAlgorithmException {
        if (StringUtils.isBlank(algorithm)) {
            throw new NullPointerException("algorithm name is null");
        }
        if (keysize <= 0) {
            throw new IllegalArgumentException("keysize should be great than zero.");
        }
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(algorithm);
        keyPairGenerator.initialize(keysize);
        return keyPairGenerator.generateKeyPair();
    }

    /**
     * 生成密钥对
     * @param algorithm
     * @param keySize
     * @param random
     * @return
     * @throws NoSuchAlgorithmException
     */
    public static KeyPair generateKeyPair(String algorithm, int keySize, SecureRandom random) throws NoSuchAlgorithmException {
        if (StringUtils.isBlank(algorithm)) {
            throw new NullPointerException("algorithm name is null");
        }
        if (keySize <= 0) {
            throw new IllegalArgumentException("keysize should be great than zero.");
        }
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(algorithm);
        keyPairGenerator.initialize(keySize, random);
        return keyPairGenerator.generateKeyPair();
    }

    /**
     * 获取RSA密钥对
     * @param keySize
     * @return
     * @throws NoSuchAlgorithmException
     */
    public static KeyPair rsaKeyPair(int keySize) throws NoSuchAlgorithmException {
        return generateKeyPair(AlgorithmEnum.RSA.value(), keySize);
    }

    /**
     * 获取RSA密钥对
     * @param keySize
     * @param random
     * @return
     * @throws NoSuchAlgorithmException
     */
    public static KeyPair rsaKeyPair(int keySize, SecureRandom random) throws NoSuchAlgorithmException {
        return generateKeyPair(AlgorithmEnum.RSA.value(), keySize, random);
    }

    /**
     * 获取DSA密钥对
     * @param keySize
     * @return
     * @throws NoSuchAlgorithmException
     */
    public static KeyPair dsaKeyPair(int keySize) throws NoSuchAlgorithmException {
        return generateKeyPair(AlgorithmEnum.DSA.value(), keySize);
    }

    /**
     * 获取DSA密钥对
     * @param keySize
     * @param random
     * @return
     * @throws NoSuchAlgorithmException
     */
    public static KeyPair dsaKeyPair(int keySize, SecureRandom random) throws NoSuchAlgorithmException {
        return generateKeyPair(AlgorithmEnum.DSA.value(), keySize, random);
    }

    /**
     * 获取DSA密钥对
     * @param key
     * @return
     */
    public static String keyToString(Key key) {
        return Base64.encodeBase64String(key.getEncoded());
    }

    /**
     * 非对称加密
     * @param algorithm
     * @param key
     * @param value
     * @return
     * @throws NoSuchAlgorithmException
     * @throws NoSuchPaddingException
     * @throws InvalidKeyException
     * @throws IllegalBlockSizeException
     * @throws BadPaddingException
     * @throws UnsupportedEncodingException
     */
    public static byte[] encryptNonSymmetric(String algorithm, Key key, String value) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, UnsupportedEncodingException {
        if (StringUtils.isBlank(algorithm)) {
            throw new NullPointerException("algorithm name is null");
        }
        if (StringUtils.isBlank(value)) {
            throw new NullPointerException("encrypt value is null");
        }
        Cipher cipher = Cipher.getInstance(algorithm);
        cipher.init(Cipher.ENCRYPT_MODE, key);
        return cipher.doFinal(value.getBytes(CHARSET_UTF8));
    }

    /**
     * 非对称加密
     * @param algorithm
     * @param key
     * @param value
     * @return
     * @throws NoSuchAlgorithmException
     * @throws NoSuchPaddingException
     * @throws InvalidKeyException
     * @throws IllegalBlockSizeException
     * @throws BadPaddingException
     * @throws UnsupportedEncodingException
     */
    public static String encryptNonSymmetricString(String algorithm, Key key, String value) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, UnsupportedEncodingException {
        byte[] bytes = encryptNonSymmetric(algorithm, key, value);
        return Base64.encodeBase64String(bytes);
    }

    /**
     * 非对称解密
     * @param algorithm
     * @param key
     * @param value
     * @return
     * @throws NoSuchAlgorithmException
     * @throws NoSuchPaddingException
     * @throws InvalidKeyException
     * @throws IllegalBlockSizeException
     * @throws BadPaddingException
     * @throws UnsupportedEncodingException
     */
    public static byte[] decryptNonSymmetric(String algorithm, Key key, String value) throws NoSuchAlgorithmException, NoSuchPaddingException,
            InvalidKeyException, IllegalBlockSizeException, BadPaddingException, UnsupportedEncodingException {
        if (StringUtils.isBlank(algorithm)) {
            throw new NullPointerException("algorithm name is null");
        }
        if (StringUtils.isBlank(value)) {
            throw new NullPointerException("decrypt value is null");
        }
        byte[] bytes = Base64.decodeBase64(value);
        Cipher cipher = Cipher.getInstance(algorithm);
        cipher.init(Cipher.DECRYPT_MODE, key);
        return cipher.doFinal(bytes);
    }

    /**
     * 非对称解密
     * @param algorithm
     * @param key
     * @param value
     * @return
     * @throws NoSuchAlgorithmException
     * @throws NoSuchPaddingException
     * @throws InvalidKeyException
     * @throws IllegalBlockSizeException
     * @throws BadPaddingException
     * @throws UnsupportedEncodingException
     */
    public static String decryptNonSymmetricString(String algorithm, Key key, String value) throws NoSuchAlgorithmException,
            NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, UnsupportedEncodingException {
        byte[] decryptBytes = decryptNonSymmetric(algorithm, key, value);
        return new String(decryptBytes, CHARSET_UTF8);
    }

    /**
     * RSA加密
     * @param key
     * @param value
     * @return
     * @throws InvalidKeyException
     * @throws NoSuchAlgorithmException
     * @throws NoSuchPaddingException
     * @throws IllegalBlockSizeException
     * @throws BadPaddingException
     * @throws UnsupportedEncodingException
     */
    public static byte[] rsaEncrypt(Key key, String value) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException,
            IllegalBlockSizeException, BadPaddingException, UnsupportedEncodingException {
        return encryptNonSymmetric(AlgorithmEnum.RSA.value(), key, value);
    }

    /**
     * RSA加密
     * @param key
     * @param value
     * @return
     * @throws InvalidKeyException
     * @throws NoSuchAlgorithmException
     * @throws NoSuchPaddingException
     * @throws IllegalBlockSizeException
     * @throws BadPaddingException
     * @throws UnsupportedEncodingException
     */
    public static String rsaEncryptString(Key key, String value) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException,
            IllegalBlockSizeException, BadPaddingException, UnsupportedEncodingException {
        return encryptNonSymmetricString(AlgorithmEnum.RSA.value(), key, value);
    }

    /**
     * RSA解密
     * @param key
     * @param value
     * @return
     * @throws InvalidKeyException
     * @throws NoSuchAlgorithmException
     * @throws NoSuchPaddingException
     * @throws IllegalBlockSizeException
     * @throws BadPaddingException
     * @throws UnsupportedEncodingException
     */
    public static byte[] rsaDecrypt(Key key, String value) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException,
            IllegalBlockSizeException, BadPaddingException, UnsupportedEncodingException {
        return decryptNonSymmetric(AlgorithmEnum.RSA.value(), key, value);
    }

    /**
     * RSA解密
     * @param key
     * @param value
     * @return
     * @throws InvalidKeyException
     * @throws NoSuchAlgorithmException
     * @throws NoSuchPaddingException
     * @throws IllegalBlockSizeException
     * @throws BadPaddingException
     * @throws UnsupportedEncodingException
     */
    public static String rsaDecryptString(Key key, String value) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException,
            IllegalBlockSizeException, BadPaddingException, UnsupportedEncodingException {
        return decryptNonSymmetricString(AlgorithmEnum.RSA.value(), key, value);
    }

    /**
     * DSA加密
     * @param key
     * @param value
     * @return
     * @throws InvalidKeyException
     * @throws NoSuchAlgorithmException
     * @throws NoSuchPaddingException
     * @throws IllegalBlockSizeException
     * @throws BadPaddingException
     * @throws UnsupportedEncodingException
     */
    public static byte[] dsaEncrypt(Key key, String value) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException,
            IllegalBlockSizeException, BadPaddingException, UnsupportedEncodingException {
        return encryptNonSymmetric(AlgorithmEnum.DSA.value(), key, value);
    }

    /**
     * DSA加密
     * @param key
     * @param value
     * @return 加密后的字符串
     * @throws InvalidKeyException
     * @throws NoSuchAlgorithmException
     * @throws NoSuchPaddingException
     * @throws IllegalBlockSizeException
     * @throws BadPaddingException
     * @throws UnsupportedEncodingException
     */
    public static String dsaEncryptString(Key key, String value) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException,
            IllegalBlockSizeException, BadPaddingException, UnsupportedEncodingException {
        return encryptNonSymmetricString(AlgorithmEnum.DSA.value(), key, value);
    }

    /**
     * DSA解密
     * @param algorithm
     * @param key
     * @param value
     * @return
     * @throws InvalidKeyException
     * @throws NoSuchAlgorithmException
     * @throws NoSuchPaddingException
     * @throws IllegalBlockSizeException
     * @throws BadPaddingException
     * @throws UnsupportedEncodingException
     */
    public static byte[] dsaDecrypt(String algorithm, Key key, String value) throws InvalidKeyException, NoSuchAlgorithmException,
            NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, UnsupportedEncodingException {
        return decryptNonSymmetric(AlgorithmEnum.DSA.value(), key, value);
    }

    /**
     * DSA解密为字符串
     * @param algorithm
     * @param key
     * @param value
     * @return 解密后的字符串
     * @throws InvalidKeyException
     * @throws NoSuchAlgorithmException
     * @throws NoSuchPaddingException
     * @throws IllegalBlockSizeException
     * @throws BadPaddingException
     * @throws UnsupportedEncodingException
     */
    public static String dsaDecryptString(String algorithm, Key key, String value) throws InvalidKeyException, NoSuchAlgorithmException,
            NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, UnsupportedEncodingException {
        return decryptNonSymmetricString(AlgorithmEnum.DSA.value(), key, value);
    }

    /**
     * 解析私钥
     * @param keyFactory
     * @param bytes
     * @return
     * @throws InvalidKeySpecException
     */
    public static PrivateKey parsePrivateKey(KeyFactory keyFactory, byte[] bytes) throws InvalidKeySpecException {
        // Creates a new PKCS8EncodedKeySpec with the given encoded key.
        // PKCS8EncodedKeySpec represents the ASN.1 encoding of a private key, encoded according to the ASN.1 type PrivateKeyInfo.
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(bytes);
        PrivateKey key = (PrivateKey) keyFactory.generatePrivate(keySpec);
        return key;
    }

    /**
     * 解析私钥
     * @param keyFactory
     * @param keyValue
     * @return
     * @throws InvalidKeySpecException
     */
    public static PrivateKey parsePrivateKey(KeyFactory keyFactory, String keyValue) throws InvalidKeySpecException {
        return parsePrivateKey(keyFactory, Base64.decodeBase64(keyValue.getBytes()));
    }

    /**
     * 解析私钥(RSA)
     * @param bytes
     * @return
     * @throws InvalidKeySpecException
     * @throws NoSuchAlgorithmException
     */
    public static PrivateKey parseRSAPrivateKey(byte[] bytes) throws InvalidKeySpecException, NoSuchAlgorithmException {
        KeyFactory keyFactory = getKeyFactory(AlgorithmEnum.RSA.value());
        return parsePrivateKey(keyFactory, bytes);
    }

    /**
     * 解析私钥(RSA)
     * @param keyValue
     * @return
     * @throws InvalidKeySpecException
     * @throws NoSuchAlgorithmException
     */
    public static PrivateKey parseRSAPrivateKey(String keyValue) throws InvalidKeySpecException, NoSuchAlgorithmException {
        KeyFactory keyFactory = getKeyFactory(AlgorithmEnum.RSA.value());
        return parsePrivateKey(keyFactory, keyValue);
    }

    /**
     * 解析私钥(DSA)
     * @param bytes
     * @return
     * @throws InvalidKeySpecException
     * @throws NoSuchAlgorithmException
     */
    public static PrivateKey parseDSAPrivateKey(byte[] bytes) throws InvalidKeySpecException, NoSuchAlgorithmException {
        KeyFactory keyFactory = getKeyFactory(AlgorithmEnum.DSA.value());
        return parsePrivateKey(keyFactory, bytes);
    }

    /**
     * 解析私钥(DSA)
     * @param keyValue
     * @return
     * @throws InvalidKeySpecException
     * @throws NoSuchAlgorithmException
     */
    public static PrivateKey parseDSAPrivateKey(String keyValue) throws InvalidKeySpecException, NoSuchAlgorithmException {
        KeyFactory keyFactory = getKeyFactory(AlgorithmEnum.DSA.value());
        return parsePrivateKey(keyFactory, keyValue);
    }

    /**
     * 解析公钥
     * @param keyFactory
     * @param bytes
     * @return
     * @throws InvalidKeySpecException
     */
    public static PublicKey parsePublicKey(KeyFactory keyFactory, byte[] bytes) throws InvalidKeySpecException {
        // Creates a new X509EncodedKeySpec with the given encoded key.
        // X509EncodedKeySpec represents the ASN.1 encoding of a public key, encoded according to the ASN.1 type SubjectPublicKeyInfo.
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(bytes);
        PublicKey key = keyFactory.generatePublic(keySpec);
        return key;
    }

    /**
     * 解析公钥
     * @param keyFactory
     * @param keyValue
     * @return
     * @throws InvalidKeySpecException
     */
    public static PublicKey parsePublicKey(KeyFactory keyFactory, String keyValue) throws InvalidKeySpecException {
        return parsePublicKey(keyFactory, Base64.decodeBase64(keyValue.getBytes()));
    }

    /**
     * 解析公钥(RSA)
     * @param bytes
     * @return 公钥
     * @throws InvalidKeySpecException
     * @throws NoSuchAlgorithmException
     */
    public static PublicKey parseRSAPublicKey(byte[] bytes) throws InvalidKeySpecException, NoSuchAlgorithmException {
        KeyFactory keyFactory = getKeyFactory(AlgorithmEnum.RSA.value());
        return parsePublicKey(keyFactory, bytes);
    }

    /**
     * 解析公钥(RSA)
     * @param keyValue
     * @return
     * @throws InvalidKeySpecException
     * @throws NoSuchAlgorithmException
     */
    public static PublicKey parseRSAPublicKey(String keyValue) throws InvalidKeySpecException, NoSuchAlgorithmException {
        KeyFactory keyFactory = getKeyFactory(AlgorithmEnum.RSA.value());
        return parsePublicKey(keyFactory, keyValue);
    }

    /**
     * 解析公钥(DSA)
     * @param bytes
     * @return 公钥
     * @throws InvalidKeySpecException
     * @throws NoSuchAlgorithmException
     */
    public static PublicKey parseDSAPublicKey(byte[] bytes) throws InvalidKeySpecException, NoSuchAlgorithmException {
        KeyFactory keyFactory = getKeyFactory(AlgorithmEnum.DSA.value());
        return parsePublicKey(keyFactory, bytes);
    }

    /**
     * @param keyValue
     * @return 公钥
     * @throws InvalidKeySpecException
     * @throws NoSuchAlgorithmException
     */
    public static PublicKey parseDSAPublicKey(String keyValue) throws InvalidKeySpecException, NoSuchAlgorithmException {
        KeyFactory keyFactory = getKeyFactory(AlgorithmEnum.DSA.value());
        return parsePublicKey(keyFactory, keyValue);
    }


    /**
     * 获取秘钥工厂类
     * @param algorithm 请求密钥算法的名称
     * @return 密钥工厂
     * @throws NoSuchAlgorithmException
     */
    public static KeyFactory getKeyFactory(String algorithm) throws NoSuchAlgorithmException {
        return KeyFactory.getInstance(algorithm);
    }

    /**
     * 获取秘钥工厂类(RSA)
     * @return
     * @throws NoSuchAlgorithmException
     */
    public static KeyFactory getRSAKeyFactory() throws NoSuchAlgorithmException {
        return getKeyFactory(AlgorithmEnum.RSA.value());
    }

    /**
     * 获取秘钥工厂类(DSA)
     * @return 密钥工厂
     * @throws NoSuchAlgorithmException
     */
    public static KeyFactory getDSAKeyFactory() throws NoSuchAlgorithmException {
        return getKeyFactory(AlgorithmEnum.DSA.value());
    }
}
