package cn.erika.util.security;

import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Random;

public class DSA{
    private static final String KEY_ALGORITHM = "DSA";
    private static final String SIGN_ALGORITHM = "SHA1WITHDSA";

    public static byte[][] initKey(int keySize) throws NoSuchAlgorithmException {
        byte[][] keyPair = new byte[2][];
        KeyPairGenerator generator = KeyPairGenerator.getInstance(KEY_ALGORITHM);

        SecureRandom random = new SecureRandom();
        random.setSeed(new Random().nextLong());

        generator.initialize(keySize, random);

        KeyPair pair = generator.genKeyPair();
        keyPair[0] = pair.getPublic().getEncoded();
        keyPair[1] = pair.getPrivate().getEncoded();

        return keyPair;
    }

    public static PublicKey getPublicKey(byte[] key) throws NoSuchAlgorithmException, InvalidKeySpecException {
        KeyFactory factory = KeyFactory.getInstance(KEY_ALGORITHM);
        X509EncodedKeySpec spec = new X509EncodedKeySpec(key);
        return factory.generatePublic(spec);
    }

    public static PrivateKey getPrivateKey(byte[] key) throws NoSuchAlgorithmException, InvalidKeySpecException {
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(key);
        KeyFactory factory = KeyFactory.getInstance(KEY_ALGORITHM);
        return factory.generatePrivate(spec);
    }

    public static byte[] sign(byte[] data, PrivateKey key) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        Signature sign = Signature.getInstance(SIGN_ALGORITHM);
        sign.initSign(key);
        sign.update(data);
        return sign.sign();
    }

    public static boolean verify(byte[] data, PublicKey key, byte[] signData) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        Signature sign = Signature.getInstance(SIGN_ALGORITHM);
        sign.initVerify(key);
        sign.update(data);
        return sign.verify(signData);
    }
}
