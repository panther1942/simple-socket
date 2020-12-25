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

public class SecurityUtils {
    private static final String PADDING_METHOD = "PKCS5Padding";
    private static final String SECURE_RANDOM = "SHA1PRNG";

    private static final AsymmetricAlgorithm DEFAULT_ASYMMETRIC_ALGORITHM = AsymmetricAlgorithm.RSA;
    private static final int DEFAULT_ASYMMETRIC_LENGTH = 2048;

    /**
     * 生成不对称密钥对
     *
     * @param keyLength 密钥长度
     * @return 返回一个装有私钥和公钥的数组 0为公钥 1为私钥
     * @throws NoSuchAlgorithmException 当前环境不支持KEY_ALGORITHM所标识的加密方法时抛出该异常
     */
    public static byte[][] initKey(AsymmetricAlgorithm algorithm, int keyLength) throws NoSuchAlgorithmException {
        byte[][] keyPair = new byte[2][];
        KeyPairGenerator generator = KeyPairGenerator.getInstance(algorithm.getValue());
        generator.initialize(keyLength);
        KeyPair pair = generator.generateKeyPair();

        keyPair[0] = pair.getPublic().getEncoded();
        keyPair[1] = pair.getPrivate().getEncoded();
        return keyPair;
    }

    public static byte[][] initKey() throws NoSuchAlgorithmException {
        return initKey(DEFAULT_ASYMMETRIC_ALGORITHM, DEFAULT_ASYMMETRIC_LENGTH);
    }

    public static byte[][] initKey(String publicKey, String privateKey) throws SecurityException {
        if (!StringUtils.isEmpty(publicKey) && !StringUtils.isEmpty(privateKey)) {
            byte[][] keyPair = new byte[2][];
            keyPair[0] = Base64.getDecoder().decode(publicKey);
            keyPair[1] = Base64.getDecoder().decode(privateKey);
            return keyPair;
        } else {
            throw new SecurityException("公钥与私钥不能为空");
        }
    }

    /**
     * 调用jdk的加密方法加密数据
     *
     * @param data     需要加密的数据
     * @param password 密码
     * @return 加密后的数据
     * @throws SecurityException 如果密钥无效或者不支持加密方式，则抛出该异常
     */
    public static byte[] encrypt(byte[] data, String password, SecurityAlgorithm algorithm) throws SecurityException {
        return encrypt(data, password, algorithm, null);
    }

    public static byte[] encrypt(byte[] data, String password, SecurityAlgorithm algorithm, byte[] iv) throws SecurityException {
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
     * 调用jdk的加密方式解密数据
     *
     * @param data     需要解密的数据
     * @param password 密码
     * @return 解密后的数据
     * @throws SecurityException 如果密钥无效或者不支持加密方式，则抛出该异常
     */
    public static byte[] decrypt(byte[] data, String password, SecurityAlgorithm algorithm) throws SecurityException {
        return decrypt(data, password, algorithm, null);
    }

    public static byte[] decrypt(byte[] data, String password, SecurityAlgorithm algorithm, byte[] iv) throws SecurityException {
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

    public static byte[] encrypt(byte[] data, byte[] key) {
        return encrypt(data, key, DEFAULT_ASYMMETRIC_ALGORITHM);
    }

    /**
     * 加密数据
     *
     * @param data 要加密的数据
     * @param key  私钥
     * @return 返回加密后的数据
     * @throws SecurityException 1、当前环境不支持该加密方法 2、秘钥无效 3、秘钥不匹配
     */
    public static byte[] encrypt(byte[] data, byte[] key, AsymmetricAlgorithm algorithm) throws SecurityException {
        return doFinal(Cipher.ENCRYPT_MODE, data, key, algorithm);
    }

    public static byte[] decrypt(byte[] data, byte[] key) {
        return decrypt(data, key, DEFAULT_ASYMMETRIC_ALGORITHM);
    }

    /**
     * 解密数据
     *
     * @param data 要解密的数据
     * @param key  公钥
     * @return 返回解密后的数据
     * @throws SecurityException 1、当前环境不支持该加密方法 2、秘钥无效 3、秘钥不匹配
     */
    public static byte[] decrypt(byte[] data, byte[] key, AsymmetricAlgorithm algorithm) throws SecurityException {
        return doFinal(Cipher.DECRYPT_MODE, data, key, algorithm);
    }

    private static byte[] doFinal(int mode, byte[] data, byte[] bKey, AsymmetricAlgorithm algorithm) {
        try {
            Key key;
            switch (mode) {
                case Cipher.ENCRYPT_MODE:
                    key = getPublicKey(bKey, algorithm);
                    break;
                case Cipher.DECRYPT_MODE:
                    key = getPrivateKey(bKey, algorithm);
                    break;
                default:
                    throw new SecurityException("不支持的模式: " + mode);
            }
            Cipher cipher = Cipher.getInstance(key.getAlgorithm());
            cipher.init(mode, key);
            return cipher.doFinal(data);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            throw new SecurityException("当前环境不支持该加密方法: " + e.getMessage());
        } catch (InvalidKeySpecException | InvalidKeyException | IllegalBlockSizeException e) {
            throw new SecurityException("秘钥无效: " + Base64.getEncoder().encodeToString(bKey));
        } catch (BadPaddingException e) {
            throw new SecurityException("秘钥不匹配: " + e.getMessage());
        }
    }

    public static byte[] sign(byte[] data, byte[] key, DigitalSignatureAlgorithm algorithm) {
        return sign(data, key, algorithm, DEFAULT_ASYMMETRIC_ALGORITHM);
    }

    public static byte[] sign(byte[] data,
                              byte[] key,
                              DigitalSignatureAlgorithm algorithm,
                              AsymmetricAlgorithm asymmetricAlgorithm) throws SecurityException {
        try {
            PrivateKey priKey = SecurityUtils.getPrivateKey(key, asymmetricAlgorithm);
            Signature sign = Signature.getInstance(algorithm.getValue());
            sign.initSign(priKey);
            sign.update(data);
            return sign.sign();
        } catch (NoSuchAlgorithmException e) {
            throw new SecurityException("当前环境不支持该加密方法: " + e.getMessage());
        } catch (InvalidKeySpecException | InvalidKeyException e) {
            throw new SecurityException("秘钥无效: " + Base64.getEncoder().encodeToString(key));
        } catch (SignatureException e) {
            throw new SecurityException("签名对象未正确初始化: " + e.getMessage());
        }
    }

    public static boolean verify(byte[] data, byte[] sign, byte[] key, DigitalSignatureAlgorithm algorithm) {
        return verify(data, sign, key, algorithm, DEFAULT_ASYMMETRIC_ALGORITHM);
    }

    public static boolean verify(byte[] data,
                                 byte[] sign,
                                 byte[] key,
                                 DigitalSignatureAlgorithm algorithm,
                                 AsymmetricAlgorithm asymmetricAlgorithm) throws SecurityException {
        try {
            PublicKey pubKey = SecurityUtils.getPublicKey(key, asymmetricAlgorithm);
            Signature signature = Signature.getInstance(algorithm.getValue());
            signature.initVerify(pubKey);
            signature.update(data);
            return signature.verify(sign);
        } catch (NoSuchAlgorithmException e) {
            throw new SecurityException("当前环境不支持该加密方法: " + e.getMessage());
        } catch (InvalidKeySpecException | InvalidKeyException e) {
            throw new SecurityException("公钥无效: " + Base64.getEncoder().encodeToString(key));
        } catch (SignatureException e) {
            throw new SecurityException("签名对象未正确初始化: " + e.getMessage());
        }
    }
}
