package cn.erika.util.security;

import cn.erika.util.string.StringUtils;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class Security {
    private static final String PADDING_METHOD = "PKCS5Padding";
    private static final String SECURE_RANDOM = "SHA1PRNG";

    /**
     * 调用jdk的AES加密方法加密数据
     *
     * @param data     需要加密的数据
     * @param password 密码
     * @return 加密后的数据
     * @throws SecurityException 如果密钥无效或者不支持AES加密方式，则抛出该异常
     */
    public static byte[] encrypt(byte[] data, SecurityAlgorithm algorithm, String password) throws SecurityException {
        return encrypt(data, algorithm, password, null);
    }

    public static byte[] encrypt(byte[] data, SecurityAlgorithm algorithm, String password, byte[] iv) throws SecurityException {
        try {
            Cipher cipher = Cipher.getInstance(algorithm.getName() + "/" + algorithm.getMode() + "/" + PADDING_METHOD);
            if (algorithm.isNeedIv()) {
                cipher.init(Cipher.ENCRYPT_MODE, getSecretKey(password, algorithm), new IvParameterSpec(iv));
            } else {
                cipher.init(Cipher.ENCRYPT_MODE, getSecretKey(password, algorithm));
            }
            return cipher.doFinal(data);
        } catch (Exception e) {
            e.printStackTrace();
            throw new SecurityException("无效的秘钥: " + password, e);
        }
    }

    /**
     * 调用jdk的AES加密方式解密数据
     *
     * @param data     需要解密的数据
     * @param password 密码
     * @return 解密后的数据
     * @throws SecurityException 如果密钥无效或者不支持AES加密方式，则抛出该异常
     */
    public static byte[] decrypt(byte[] data, SecurityAlgorithm algorithm, String password) throws SecurityException {
        return decrypt(data, algorithm, password, null);
    }

    public static byte[] decrypt(byte[] data, SecurityAlgorithm algorithm, String password, byte[] iv) throws SecurityException {
        try {
            Cipher cipher = Cipher.getInstance(algorithm.getName() + "/" + algorithm.getMode() + "/" + PADDING_METHOD);
            if (algorithm.isNeedIv()) {
                cipher.init(Cipher.DECRYPT_MODE, getSecretKey(password, algorithm), new IvParameterSpec(iv));
            } else {
                cipher.init(Cipher.DECRYPT_MODE, getSecretKey(password, algorithm));
            }
            return cipher.doFinal(data);
        } catch (Exception e) {
            e.printStackTrace();
            throw new SecurityException("无效的秘钥: " + password, e);
        }
    }

    /**
     * 带用jdk的AES加密方式生成加密对象
     *
     * @param password 密码
     * @return 加密对象
     * @throws NoSuchAlgorithmException 如果不支持AES加密方式则抛出该异常
     */
    private static SecretKeySpec getSecretKey(final String password, SecurityAlgorithm algorithm) throws NoSuchAlgorithmException {
        KeyGenerator generator = KeyGenerator.getInstance(algorithm.getName());
        SecureRandom secureRandom = SecureRandom.getInstance(SECURE_RANDOM);
        secureRandom.setSeed(password.getBytes());
        generator.init(algorithm.getSecurityLength(), secureRandom);
        SecretKey key = generator.generateKey();
        return new SecretKeySpec(key.getEncoded(), algorithm.getName());
    }

    /**
     * 通过byte数组生成原始公钥
     *
     * @param key 由原始私钥生成的byte数组
     * @return 返回原始公钥
     * @throws NoSuchAlgorithmException 当前环境不支持KEY_ALGORITHM所标识的加密方法时抛出该异常
     * @throws InvalidKeySpecException  公钥无效时抛出该异常
     */
    public static PublicKey getPublicKey(byte[] key, AsymmetricAlgorithm algorithm) throws NoSuchAlgorithmException, InvalidKeySpecException {
        KeyFactory factory = KeyFactory.getInstance(algorithm.getValue());
        X509EncodedKeySpec spec = new X509EncodedKeySpec(key);
        return factory.generatePublic(spec);
    }

    /**
     * 通过byte数组生成原始私钥
     *
     * @param key 由原始私钥生成的byte数组
     * @return 返回原始私钥
     * @throws NoSuchAlgorithmException 当前环境不支持KEY_ALGORITHM所标识的加密方法时抛出该异常
     * @throws InvalidKeySpecException  私钥无效时抛出该异常
     */
    public static PrivateKey getPrivateKey(byte[] key, AsymmetricAlgorithm algorithm) throws NoSuchAlgorithmException, InvalidKeySpecException {
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(key);
        KeyFactory factory = KeyFactory.getInstance(algorithm.getValue());
        return factory.generatePrivate(spec);
    }

    /**
     * 通过私钥加密数据
     *
     * @param data 要加密的数据
     * @param key  私钥
     * @return 返回加密后的数据
     * @throws SecurityException 1、当前环境不支持该加密方法 2、秘钥无效 3、秘钥不匹配
     */
    public static byte[] encryptByPrivateKey(byte[] data, byte[] key, AsymmetricAlgorithm algorithm) throws SecurityException {
        try {
            PrivateKey privateKey = getPrivateKey(key, algorithm);
            Cipher cipher = Cipher.getInstance(privateKey.getAlgorithm());
            cipher.init(Cipher.ENCRYPT_MODE, privateKey);
            return cipher.doFinal(data);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            throw new SecurityException("当前环境不支持该加密方法: " + e.getMessage());
        } catch (InvalidKeySpecException | InvalidKeyException | IllegalBlockSizeException e) {
            throw new SecurityException("秘钥无效: " + Base64.getEncoder().encodeToString(key));
        } catch (BadPaddingException e) {
            throw new SecurityException("秘钥不匹配: " + e.getMessage());
        }
    }

    /**
     * 通过公钥加密数据
     *
     * @param data 要加密的数据
     * @param key  公钥
     * @return 返回加密后的数据
     * @throws SecurityException 1、当前环境不支持该加密方法 2、秘钥无效 3、秘钥不匹配
     */
    public static byte[] encryptByPublicKey(byte[] data, byte[] key, AsymmetricAlgorithm algorithm) throws SecurityException {
        try {
            PublicKey publicKey = getPublicKey(key, algorithm);
            System.out.println(publicKey.getAlgorithm());
            Cipher cipher = Cipher.getInstance(publicKey.getAlgorithm());
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            return cipher.doFinal(data);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            throw new SecurityException("当前环境不支持该加密方法: " + e.getMessage());
        } catch (InvalidKeySpecException | InvalidKeyException | IllegalBlockSizeException e) {
            throw new SecurityException("秘钥无效: " + Base64.getEncoder().encodeToString(key));
        } catch (BadPaddingException e) {
            throw new SecurityException("秘钥不匹配: " + e.getMessage());
        }
    }

    /**
     * 通过私钥解密数据
     *
     * @param data 要解密的数据
     * @param key  公钥
     * @return 返回解密后的数据
     * @throws SecurityException 1、当前环境不支持该加密方法 2、秘钥无效 3、秘钥不匹配
     */
    public static byte[] decryptByPrivateKey(byte[] data, byte[] key, AsymmetricAlgorithm algorithm) throws SecurityException {
        try {
            PrivateKey privateKey = getPrivateKey(key, algorithm);
            Cipher cipher = Cipher.getInstance(privateKey.getAlgorithm());
            cipher.init(Cipher.DECRYPT_MODE, privateKey);
            return cipher.doFinal(data);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            throw new SecurityException("当前环境不支持该加密方法: " + e.getMessage());
        } catch (InvalidKeySpecException | InvalidKeyException | IllegalBlockSizeException e) {
            throw new SecurityException("秘钥无效: " + Base64.getEncoder().encodeToString(key));
        } catch (BadPaddingException e) {
            throw new SecurityException("秘钥不匹配: " + e.getMessage());
        }
    }

    /**
     * 通过公钥解密数据
     *
     * @param data 要解密的数据
     * @param key  公钥
     * @return 返回解密后的数据
     * @throws SecurityException 1、当前环境不支持该加密方法 2、秘钥无效 3、秘钥不匹配
     */
    public static byte[] decryptByPublicKey(byte[] data, byte[] key, AsymmetricAlgorithm algorithm) throws SecurityException {
        try {
            PublicKey publicKey = getPublicKey(key, algorithm);
            Cipher cipher = Cipher.getInstance(publicKey.getAlgorithm());
            cipher.init(Cipher.DECRYPT_MODE, publicKey);
            return cipher.doFinal(data);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            throw new SecurityException("当前环境不支持该加密方法: " + e.getMessage());
        } catch (InvalidKeySpecException | InvalidKeyException | IllegalBlockSizeException e) {
            throw new SecurityException("秘钥无效: " + Base64.getEncoder().encodeToString(key));
        } catch (BadPaddingException e) {
            throw new SecurityException("秘钥不匹配: " + e.getMessage());
        }
    }
}
