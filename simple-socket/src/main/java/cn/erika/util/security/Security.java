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

    /**
     * 调用jdk的AES加密方法加密数据
     *
     * @param data     需要加密的数据
     * @param password 密码
     * @return 加密后的数据
     * @throws SecurityException 如果密钥无效或者不支持AES加密方式，则抛出该异常
     */
    public static byte[] encrypt(byte[] data, Type algorithm, String password) throws SecurityException {
        return encrypt(data, algorithm, password, null);
    }

    public static byte[] encrypt(byte[] data, Type algorithm, String password, byte[] iv) throws SecurityException {
        try {
            Cipher cipher = Cipher.getInstance(algorithm.name + "/" + algorithm.mode + "/" + PADDING_METHOD);
            if (algorithm.needIv) {
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
    public static byte[] decrypt(byte[] data, Type algorithm, String password) throws SecurityException {
        return decrypt(data, algorithm, password, null);
    }

    public static byte[] decrypt(byte[] data, Type algorithm, String password, byte[] iv) throws SecurityException {
        try {
            Cipher cipher = Cipher.getInstance(algorithm.name + "/" + algorithm.mode + "/" + PADDING_METHOD);
            if (algorithm.needIv) {
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
    private static SecretKeySpec getSecretKey(final String password, Type algorithm) throws NoSuchAlgorithmException {
        KeyGenerator generator = KeyGenerator.getInstance(algorithm.name);
        SecureRandom secureRandom = SecureRandom.getInstance("SHA1PRNG");
        secureRandom.setSeed(password.getBytes());
        generator.init(algorithm.securityLength, secureRandom);
        SecretKey key = generator.generateKey();
        return new SecretKeySpec(key.getEncoded(), algorithm.name);
    }

    public enum Type {
        AES128ECB("AES128ECB", "AES", "ECB", 128, false),
        AES192ECB("AES192ECB", "AES", "ECB", 192, false),
        AES256ECB("AES256ECB", "AES", "ECB", 256, false),
        AES128CBC("AES128CBC", "AES", "CBC", 128, true),
        AES192CBC("AES192CBC", "AES", "CBC", 192, true),
        AES256CBC("AES256CBC", "AES", "CBC", 256, true),
        AES128CTR("AES128CTR", "AES", "CTR", 128, true),
        AES192CTR("AES192CTR", "AES", "CTR", 192, true),
        AES256CTR("AES256CTR", "AES", "CTR", 256, true),
        DES56ECB("DES56ECB", "DES", "ECB", 56, false),
        DES56CBC("DES56CBC", "DES", "CBC", 56, true),
        DES56CTR("DES56CTR", "DES", "CTR", 56, true),
        TDES112ECB("TDES112ECB", "TripleDES", "ECB", 112, false),
        TDES112CBC("TDES112CBC", "TripleDES", "CBC", 112, true),
        TDES112CTR("TDES112CTR", "TripleDES", "CTR", 112, true),
        TDES168ECB("TDES168ECB", "TripleDES", "ECB", 168, false),
        TDES168CBC("TDES168CBC", "TripleDES", "CBC", 168, true),
        TDES168CTR("TDES168CTR", "TripleDES", "CTR", 168, true);

        private String value;
        private String name;
        private String mode;
        private int securityLength;
        private boolean needIv;

        Type(String value, String name, String mode, int securityLength, boolean needIv) {
            this.value = value;
            this.name = name;
            this.mode = mode;
            this.securityLength = securityLength;
            this.needIv = needIv;
        }

        public String getValue() {
            return this.value;
        }

        public static Type getByName(String name) {
            for (Type type : Type.values()) {
                if (type.getValue().equals(name)) {
                    return type;
                }
            }
            return null;
        }
    }
}
