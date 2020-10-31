package cn.erika.util.security;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public class Security {
    private static final String PADDING_METHOD = "PKCS5Padding";

    /**
     * 调用jdk的AES加密方法加密数据
     *
     * @param data     需要加密的数据
     * @param password 密码
     * @return 加密后的数据
     * @throws IOException 如果密钥无效或者不支持AES加密方式，则抛出该异常
     */
    public static byte[] encrypt(byte[] data, Type algorithm, String password) throws IOException {
        return encrypt(data, algorithm, password, null);
    }

    public static byte[] encrypt(byte[] data, Type algorithm, String password, byte[] iv) throws IOException {
        try {
            Cipher cipher = Cipher.getInstance(algorithm.getAlgorithm() + "/" + algorithm.getMode() + "/" + PADDING_METHOD);
            if (!"ECB".equals(algorithm.getMode())) {
                cipher.init(Cipher.ENCRYPT_MODE, getSecretKey(password, algorithm), new IvParameterSpec(iv));
            } else {
                cipher.init(Cipher.ENCRYPT_MODE, getSecretKey(password, algorithm));
            }
            return cipher.doFinal(data);
        } catch (Exception e) {
            e.printStackTrace();
            throw new IOException("无效的秘钥: " + password);
        }
    }

    /**
     * 调用jdk的AES加密方式解密数据
     *
     * @param data     需要解密的数据
     * @param password 密码
     * @return 解密后的数据
     * @throws IOException 如果密钥无效或者不支持AES加密方式，则抛出该异常
     */
    public static byte[] decrypt(byte[] data, Type algorithm, String password) throws IOException {
        return decrypt(data, algorithm, password, null);
    }

    public static byte[] decrypt(byte[] data, Type algorithm, String password, byte[] iv) throws IOException {
        try {
            Cipher cipher = Cipher.getInstance(algorithm.getAlgorithm() + "/" + algorithm.getMode() + "/" + PADDING_METHOD);
            if (!"ECB".equals(algorithm.getMode())) {
                cipher.init(Cipher.DECRYPT_MODE, getSecretKey(password, algorithm), new IvParameterSpec(iv));
            } else {
                cipher.init(Cipher.DECRYPT_MODE, getSecretKey(password, algorithm));
            }
            return cipher.doFinal(data);
        } catch (Exception e) {
            e.printStackTrace();
            throw new IOException("无效的秘钥: " + password);
        }
    }

    /**
     * 带用jdk的AES加密方式生成加密对象
     *
     * @param password 密码
     * @return 加密对象
     * @throws NoSuchAlgorithmException 如果不支持AES加密方式则抛出该异常
     */
    private static SecretKeySpec getSecretKey(final String password, Type algorithm) throws NoSuchAlgorithmException {
        KeyGenerator generator = KeyGenerator.getInstance(algorithm.getAlgorithm());
        SecureRandom secureRandom = SecureRandom.getInstance("SHA1PRNG");
        secureRandom.setSeed(password.getBytes());
        generator.init(algorithm.getSecurityLength(), secureRandom);
        SecretKey key = generator.generateKey();
        return new SecretKeySpec(key.getEncoded(), algorithm.getAlgorithm());
    }

    public enum Type {
        AES128ECB("AES128ECB", "AES", "ECB", 128),
        AES192ECB("AES192ECB", "AES", "ECB", 192),
        AES256ECB("AES256ECB", "AES", "ECB", 256),
        AES128CBC("AES128CBC", "AES", "CBC", 128),
        AES192CBC("AES192CBC", "AES", "CBC", 192),
        AES256CBC("AES256CBC", "AES", "CBC", 256),
        AES128CTR("AES128CTR", "AES", "CTR", 128),
        AES192CTR("AES192CTR", "AES", "CTR", 192),
        AES256CTR("AES256CTR", "AES", "CTR", 256),
        DES56ECB("DES56ECB", "DES", "ECB", 56),
        DES56CBC("DES56CBC", "DES", "CBC", 56),
        DES56CTR("DES56CTR", "DES", "CTR", 56),
        TDES112ECB("TDES112ECB", "TripleDES", "ECB", 112),
        TDES112CBC("TDES112CBC", "TripleDES", "CBC", 112),
        TDES112CTR("TDES112CTR", "TripleDES", "CTR", 112),
        TDES168ECB("TDES168ECB", "TripleDES", "ECB", 168),
        TDES168CBC("TDES168CBC", "TripleDES", "CBC", 168),
        TDES168CTR("TDES168CTR", "TripleDES", "CTR", 168);

        private String name;
        private String algorithm;
        private String mode;
        private int securityLength;

        Type(String name, String algorithm, String mode, int securityLength) {
            this.name = name;
            this.algorithm = algorithm;
            this.mode = mode;
            this.securityLength = securityLength;
        }

        public String getName() {
            return name;
        }

        public String getAlgorithm() {
            return this.algorithm;
        }

        public String getMode() {
            return mode;
        }

        public int getSecurityLength() {
            return this.securityLength;
        }
    }
}
