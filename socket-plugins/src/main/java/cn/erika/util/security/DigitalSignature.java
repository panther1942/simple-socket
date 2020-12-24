package cn.erika.util.security;

import cn.erika.util.string.StringUtils;

import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;

public class DigitalSignature {
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


    public static byte[] sign(byte[] data,
                              byte[] key,
                              DigitalSignatureAlgorithm algorithm,
                              AsymmetricAlgorithm asymmetricAlgorithm) throws SecurityException {
        try {
            PrivateKey priKey = Security.getPrivateKey(key, asymmetricAlgorithm);
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

    public static boolean verify(byte[] data,
                                 byte[] key,
                                 byte[] signData,
                                 DigitalSignatureAlgorithm algorithm,
                                 AsymmetricAlgorithm asymmetricAlgorithm) throws SecurityException {
        try {
            PublicKey pubKey = Security.getPublicKey(key, asymmetricAlgorithm);
            Signature sign = Signature.getInstance(algorithm.getValue());
            sign.initVerify(pubKey);
            sign.update(data);
            return sign.verify(signData);
        } catch (NoSuchAlgorithmException e) {
            throw new SecurityException("当前环境不支持该加密方法: " + e.getMessage());
        } catch (InvalidKeySpecException | InvalidKeyException e) {
            throw new SecurityException("公钥无效: " + Base64.getEncoder().encodeToString(key));
        } catch (SignatureException e) {
            throw new SecurityException("签名对象未正确初始化: " + e.getMessage());
        }
    }
}
