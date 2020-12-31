package cn.erika.util.security;

import cn.erika.socket.exception.UnsupportedAlgorithmException;
import cn.erika.util.security.algorithm.AsymmetricAlgorithm;
import cn.erika.util.security.algorithm.DigitalSignatureAlgorithm;
import cn.erika.util.security.algorithm.SecurityAlgorithm;
import sun.security.ec.ECPrivateKeyImpl;
import sun.security.ec.ECPublicKeyImpl;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigInteger;
import java.security.*;
import java.security.interfaces.ECKey;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.security.spec.*;
import java.util.Base64;

/**
 * 加密工具类
 * 实现了对称加密算法中 AES/DES/TDES的加密与解密
 * 实现了不对称加密算法中 RSA/EC的加密与解密
 * 强烈不建议使用EC 因为JDK的EC性能太差了 等啥时候发布新的EC实现再说 或者你自己去实现
 * 现在的EC只是能用 具体好不好用 稳定性啥的 有没有坑 我没去跳 因为RSA足够我用了
 * 另外我数学不好 BASE64我都写好久 纸上画了三页才捣鼓出来 还只能搞ASCII这128个字符
 * 等我把算法实现 等我有媳妇再说吧
 */
public class SecurityUtils {
    // 填充方式设置为PKCS5Padding 可选NoPadding/PKCS1Padding/PKCS5Padding/OAEPPadding
    // 建议用PKCS5Padding 方便 省事
    private static final String PADDING_METHOD = "PKCS5Padding";
    // 种子随机生成器使用SHA1PRNG 用于获取加密/解密器
    private static final String SECURE_RANDOM = "SHA1PRNG";

    // 默认的不对称加密算法
    private static final AsymmetricAlgorithm DEFAULT_ASYMMETRIC_ALGORITHM = AsymmetricAlgorithm.RSA;
    // 默认的不对称加密长度
    private static final int DEFAULT_ASYMMETRIC_LENGTH = 2048;

    /**
     * 生成不对称密钥对
     *
     * @param algorithm 不对称加密算法
     * @param keyLength 密钥长度
     * @return 返回一个装有私钥和公钥的数组 0为公钥 1为私钥
     * @throws NoSuchAlgorithmException 当前环境不支持algorithm所标识的加密方法时抛出该异常
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

    /**
     * 使用默认算法和长度生成不对称密钥对
     *
     * @return 返回一个装有私钥和公钥的数组 0为公钥 1为私钥
     * @throws NoSuchAlgorithmException 当前环境不支持algorithm所标识的加密方法时抛出该异常
     */
    public static byte[][] initKey() throws NoSuchAlgorithmException {
        return initKey(DEFAULT_ASYMMETRIC_ALGORITHM, DEFAULT_ASYMMETRIC_LENGTH);
    }

    /**
     * 生成ECC(圆锥曲线加密算法)的密钥 鬼知道为啥网上这块的代码都一样
     *
     * @return EC算法的密钥对
     * @throws InvalidKeyException 如果初始化密钥无效则抛出该异常
     */
    public static byte[][] initEcKey() throws InvalidKeyException {
        byte[][] keyPair = new byte[2][];
        BigInteger x1 = new BigInteger(
                "2fe13c0537bbc11acaa07d793de4e6d5e5c94eee8", 16);
        BigInteger x2 = new BigInteger(
                "289070fb05d38ff58321f2e800536d538ccdaa3d9", 16);
        ECPoint g = new ECPoint(x1, x2);
        // the order of generator
        BigInteger n = new BigInteger(
                "5846006549323611672814741753598448348329118574063", 10);
        // the cofactor
        int h = 2;
        int m = 163;
        int[] ks = {7, 6, 3};
        ECFieldF2m ecField = new ECFieldF2m(m, ks);
        // y^2+xy=x^3+x^2+1
        BigInteger a = new BigInteger("1", 2);
        BigInteger b = new BigInteger("1", 2);
        EllipticCurve ellipticCurve = new EllipticCurve(ecField, a, b);
        ECParameterSpec ecParameterSpec = new ECParameterSpec(ellipticCurve, g,
                n, h);
        // 公钥
        ECPublicKey publicKey = new ECPublicKeyImpl(g, ecParameterSpec);
        BigInteger s = new BigInteger(
                "1234006549323611672814741753598448348329118574063", 10);
        // 私钥
        ECPrivateKey privateKey = new ECPrivateKeyImpl(s, ecParameterSpec);
        keyPair[0] = publicKey.getEncoded();
        keyPair[1] = privateKey.getEncoded();
        return keyPair;
    }

    /**
     * 使用对称加密算法加密数据
     *
     * @param data      需要加密的数据
     * @param password  密码
     * @param algorithm 对称加密算法
     * @return 加密后的数据
     * @throws UnsupportedAlgorithmException 如果不支持加密方式或者加密出错，则抛出该异常
     * @throws InvalidKeyException           如果密钥无效则抛出该异常
     */
    public static byte[] encrypt(byte[] data, String password, SecurityAlgorithm algorithm)
            throws UnsupportedAlgorithmException, InvalidKeyException {
        return encrypt(data, password, algorithm, null);
    }

    /**
     * 使用对称加密算法加密数据
     *
     * @param data      需要加密的数据
     * @param password  密码
     * @param algorithm 对称加密算法
     * @param iv        向量
     * @return 加密后的数据
     * @throws UnsupportedAlgorithmException 如果不支持加密方式或者加密出错，则抛出该异常
     * @throws InvalidKeyException           如果密钥无效则抛出该异常
     */
    public static byte[] encrypt(byte[] data, String password, SecurityAlgorithm algorithm, byte[] iv)
            throws UnsupportedAlgorithmException, InvalidKeyException {
        try {
            Cipher cipher = Cipher.getInstance(algorithm.getName() + "/" + algorithm.getMode() + "/" + PADDING_METHOD);
            if (algorithm.isNeedIv()) {
                cipher.init(Cipher.ENCRYPT_MODE, getSecretKey(password, algorithm), new IvParameterSpec(iv));
            } else {
                cipher.init(Cipher.ENCRYPT_MODE, getSecretKey(password, algorithm));
            }
            return cipher.doFinal(data);
        } catch (NoSuchAlgorithmException | InvalidAlgorithmParameterException e) {
            throw new UnsupportedAlgorithmException("不支持的加密方法: " + e.getMessage());
        } catch (NoSuchPaddingException | BadPaddingException | IllegalBlockSizeException e) {
            throw new UnsupportedAlgorithmException("加密出错: " + e.getMessage());
        }
    }

    /**
     * 使用对称加密算法解密数据
     *
     * @param data      需要解密的数据
     * @param password  密码
     * @param algorithm 对称加密算法
     * @return 解密后的数据
     * @throws UnsupportedAlgorithmException 如果不支持加密方式或者加密出错，则抛出该异常
     * @throws InvalidKeyException           如果密钥无效则抛出该异常
     */
    public static byte[] decrypt(byte[] data, String password, SecurityAlgorithm algorithm)
            throws InvalidKeyException, UnsupportedAlgorithmException {
        return decrypt(data, password, algorithm, null);
    }

    /**
     * 使用对称加密算法解密数据
     *
     * @param data      需要解密的数据
     * @param password  密码
     * @param algorithm 对称加密算法
     * @param iv        向量
     * @return 解密后的数据
     * @throws UnsupportedAlgorithmException 如果不支持加密方式或者加密出错，则抛出该异常
     * @throws InvalidKeyException           如果密钥无效则抛出该异常
     */
    public static byte[] decrypt(byte[] data, String password, SecurityAlgorithm algorithm, byte[] iv)
            throws UnsupportedAlgorithmException, InvalidKeyException {
        try {
            Cipher cipher = Cipher.getInstance(algorithm.getName() + "/" + algorithm.getMode() + "/" + PADDING_METHOD);
            if (algorithm.isNeedIv()) {
                cipher.init(Cipher.DECRYPT_MODE, getSecretKey(password, algorithm), new IvParameterSpec(iv));
            } else {
                cipher.init(Cipher.DECRYPT_MODE, getSecretKey(password, algorithm));
            }
            return cipher.doFinal(data);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidAlgorithmParameterException e) {
            throw new UnsupportedAlgorithmException("不支持的加密方法: " + e.getMessage());
        } catch (BadPaddingException | IllegalBlockSizeException e) {
            throw new UnsupportedAlgorithmException("加密出错: " + e.getMessage());
        }
    }

    /**
     * 对称加密算法获取加密对象
     *
     * @param password 密码
     * @return 加密对象
     * @throws NoSuchAlgorithmException 如果不支持AES加密方式则抛出该异常
     */
    private static SecretKeySpec getSecretKey(final String password, SecurityAlgorithm algorithm)
            throws NoSuchAlgorithmException {
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
     * @throws NoSuchAlgorithmException 当前环境不支持algorithm所标识的加密方法时抛出该异常
     * @throws InvalidKeySpecException  公钥无效时抛出该异常
     */
    private static PublicKey getPublicKey(byte[] key, AsymmetricAlgorithm algorithm)
            throws NoSuchAlgorithmException, InvalidKeySpecException {
        KeyFactory factory = KeyFactory.getInstance(algorithm.getValue());
        X509EncodedKeySpec spec = new X509EncodedKeySpec(key);
        return factory.generatePublic(spec);
    }

    /**
     * 通过byte数组生成原始私钥
     *
     * @param key 由原始私钥生成的byte数组
     * @return 返回原始私钥
     * @throws NoSuchAlgorithmException 当前环境不支持algorithm所标识的加密方法时抛出该异常
     * @throws InvalidKeySpecException  私钥无效时抛出该异常
     */
    private static PrivateKey getPrivateKey(byte[] key, AsymmetricAlgorithm algorithm)
            throws NoSuchAlgorithmException, InvalidKeySpecException {
        KeyFactory factory = KeyFactory.getInstance(algorithm.getValue());
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(key);
        return factory.generatePrivate(spec);
    }

    /**
     * 加密数据 使用默认的加密算法
     *
     * @param data 要加密的数据
     * @param key  私钥
     * @return 返回加密后的数据
     * @throws InvalidKeyException           公钥无效时抛出该异常
     * @throws UnsupportedAlgorithmException 当前环境不支持algorithm所标识的加密方法时抛出该异常
     */
    public static byte[] encrypt(byte[] data, byte[] key)
            throws InvalidKeyException, UnsupportedAlgorithmException {
        return encrypt(data, key, DEFAULT_ASYMMETRIC_ALGORITHM);
    }

    /**
     * 加密数据
     *
     * @param data      要加密的数据
     * @param key       私钥
     * @param algorithm 不对称加密算法
     * @return 返回加密后的数据
     * @throws InvalidKeyException           公钥无效时抛出该异常
     * @throws UnsupportedAlgorithmException 当前环境不支持algorithm所标识的加密方法时抛出该异常
     */
    public static byte[] encrypt(byte[] data, byte[] key, AsymmetricAlgorithm algorithm)
            throws InvalidKeyException, UnsupportedAlgorithmException {
        return doFinal(Cipher.ENCRYPT_MODE, data, key, algorithm);
    }

    /**
     * 解密数据 使用默认的加密算法
     *
     * @param data 要解密的数据
     * @param key  公钥
     * @return 返回解密后的数据
     * @throws InvalidKeyException           私钥无效时抛出该异常
     * @throws UnsupportedAlgorithmException 当前环境不支持algorithm所标识的加密方法时抛出该异常
     */
    public static byte[] decrypt(byte[] data, byte[] key)
            throws InvalidKeyException, UnsupportedAlgorithmException {
        return decrypt(data, key, DEFAULT_ASYMMETRIC_ALGORITHM);
    }

    /**
     * 解密数据
     *
     * @param data      要解密的数据
     * @param key       公钥
     * @param algorithm 不对称加密算法
     * @return 返回解密后的数据
     * @throws InvalidKeyException           私钥无效时抛出该异常
     * @throws UnsupportedAlgorithmException 当前环境不支持algorithm所标识的加密方法时抛出该异常
     */
    public static byte[] decrypt(byte[] data,
                                 byte[] key,
                                 AsymmetricAlgorithm algorithm)
            throws InvalidKeyException, UnsupportedAlgorithmException {
        return doFinal(Cipher.DECRYPT_MODE, data, key, algorithm);
    }


    /**
     * 加密或者解密数据
     *
     * @param data      要解密的数据
     * @param bKey      密钥
     * @param algorithm 不对称加密算法
     * @return 返回解密后的数据
     * @throws InvalidKeyException           密钥无效时抛出该异常
     * @throws UnsupportedAlgorithmException 当前环境不支持algorithm所标识的加密方法时抛出该异常
     */
    private static byte[] doFinal(int mode,
                                  byte[] data,
                                  byte[] bKey,
                                  AsymmetricAlgorithm algorithm)
            throws UnsupportedAlgorithmException, InvalidKeyException {
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
                    throw new UnsupportedAlgorithmException("不支持的模式: " + mode);
            }
            Cipher cipher = Cipher.getInstance(key.getAlgorithm());
            cipher.init(mode, key);
            return cipher.doFinal(data);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            throw new UnsupportedAlgorithmException("当前环境不支持该加密方法: " + e.getMessage());
        } catch (BadPaddingException | IllegalBlockSizeException | InvalidKeySpecException e) {
            throw new UnsupportedAlgorithmException("加密出错: " + e.getMessage());
        }
    }

    /**
     * 数字签名的签名方法
     *
     * @param data      要签名的数据
     * @param key       私钥
     * @param algorithm 数字签名算法
     * @return 签名
     * @throws InvalidKeyException           私钥无效
     * @throws UnsupportedAlgorithmException 当前环境不支持algorithm所标识的加密方法时抛出该异常
     */
    public static byte[] sign(byte[] data,
                              byte[] key,
                              DigitalSignatureAlgorithm algorithm)
            throws InvalidKeyException, UnsupportedAlgorithmException {
        return sign(data, key, algorithm, DEFAULT_ASYMMETRIC_ALGORITHM);
    }

    /**
     * 数字签名的签名方法
     *
     * @param data                要签名的数据
     * @param key                 私钥
     * @param algorithm           数字签名算法
     * @param asymmetricAlgorithm 不对称加密算法
     * @return 签名
     * @throws InvalidKeyException           私钥无效
     * @throws UnsupportedAlgorithmException 当前环境不支持algorithm所标识的加密方法时抛出该异常
     */
    public static byte[] sign(byte[] data,
                              byte[] key,
                              DigitalSignatureAlgorithm algorithm,
                              AsymmetricAlgorithm asymmetricAlgorithm)
            throws UnsupportedAlgorithmException, InvalidKeyException {
        try {
            PrivateKey priKey = getPrivateKey(key, asymmetricAlgorithm);
            Signature sign = Signature.getInstance(algorithm.getValue());
            sign.initSign(priKey);
            sign.update(data);
            return sign.sign();
        } catch (NoSuchAlgorithmException e) {
            throw new UnsupportedAlgorithmException("当前环境不支持该加密方法: " + e.getMessage());
        } catch (InvalidKeySpecException | InvalidKeyException e) {
            throw new InvalidKeyException("秘钥无效: " + Base64.getEncoder().encodeToString(key));
        } catch (SignatureException e) {
            throw new RuntimeException("签名对象未正确初始化: " + e.getMessage());
        }
    }

    /**
     * 数字签名的验证方法
     *
     * @param data      要签名的数据
     * @param key       公钥
     * @param algorithm 数字签名算法
     * @return 签名
     * @throws InvalidKeyException           公钥无效
     * @throws UnsupportedAlgorithmException 当前环境不支持algorithm所标识的加密方法时抛出该异常
     */
    public static boolean verify(byte[] data,
                                 byte[] sign,
                                 byte[] key,
                                 DigitalSignatureAlgorithm algorithm)
            throws InvalidKeyException, UnsupportedAlgorithmException {
        return verify(data, sign, key, algorithm, DEFAULT_ASYMMETRIC_ALGORITHM);
    }

    /**
     * 数字签名的验证方法
     *
     * @param data                要签名的数据
     * @param key                 公钥
     * @param algorithm           数字签名算法
     * @param asymmetricAlgorithm 不对称加密算法
     * @return 签名
     * @throws InvalidKeyException           公钥无效
     * @throws UnsupportedAlgorithmException 当前环境不支持algorithm所标识的加密方法时抛出该异常
     */
    public static boolean verify(byte[] data,
                                 byte[] sign,
                                 byte[] key,
                                 DigitalSignatureAlgorithm algorithm,
                                 AsymmetricAlgorithm asymmetricAlgorithm)
            throws UnsupportedAlgorithmException, InvalidKeyException {
        try {
            PublicKey pubKey = getPublicKey(key, asymmetricAlgorithm);
            Signature signature = Signature.getInstance(algorithm.getValue());
            signature.initVerify(pubKey);
            signature.update(data);
            return signature.verify(sign);
        } catch (NoSuchAlgorithmException e) {
            throw new UnsupportedAlgorithmException("当前环境不支持该加密方法: " + e.getMessage());
        } catch (InvalidKeySpecException | InvalidKeyException e) {
            throw new InvalidKeyException("秘钥无效: " + Base64.getEncoder().encodeToString(key));
        } catch (SignatureException e) {
            throw new RuntimeException("签名对象未正确初始化: " + e.getMessage());
        }
    }
}
