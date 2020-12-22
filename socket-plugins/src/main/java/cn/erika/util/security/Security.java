package cn.erika.util.security;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

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
}
