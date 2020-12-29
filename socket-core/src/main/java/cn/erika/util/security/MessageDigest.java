package cn.erika.util.security;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.Charset;
import java.security.NoSuchAlgorithmException;
import java.util.zip.CRC32;

public class MessageDigest {

    public static byte[] sum(String data, MessageDigestAlgorithm algorithm) throws SecurityException {
        return sum(data, Charset.forName("UTF-8"), algorithm);
    }

    public static byte[] sum(String data, Charset charset, MessageDigestAlgorithm algorithm) throws SecurityException {
        return sum(data.getBytes(charset), algorithm);
    }

    public static byte[] sum(File file, MessageDigestAlgorithm algorithm) throws IOException {
        try {
            java.security.MessageDigest digest = java.security.MessageDigest.getInstance(algorithm.getValue());
            try (FileInputStream in = new FileInputStream(file)) {
                int length = 0;
                byte[] data = new byte[4096];

                while ((length = in.read(data)) != -1) {
                    digest.update(data, 0, length);
                }
                return digest.digest();
            }
        } catch (NoSuchAlgorithmException e) {
            throw new SecurityException("当前系统不支持该算法: " + algorithm, e);
        }
    }

    public static byte[] sum(byte[] data, MessageDigestAlgorithm algorithm) throws SecurityException {
        if (data == null) {
            throw new IllegalArgumentException("不能对空值签名");
        }
        try {
            java.security.MessageDigest digest = java.security.MessageDigest.getInstance(algorithm.getValue());
            digest.update(data);
            return digest.digest();
        } catch (NoSuchAlgorithmException e) {
            throw new SecurityException("当前系统不支持该算法: " + algorithm, e);
        }
    }

    public static long crc32Sum(byte[] data) {
        CRC32 crc = new CRC32();
        crc.update(data);
        return crc.getValue();
    }

    public static long crc32Sum(File file) throws IOException {
        try (RandomAccessFile in = new RandomAccessFile(file,"r")) {
            int length = 0;
            byte[] data = new byte[4096];
            CRC32 crc = new CRC32();
            while ((length = in.read(data)) != -1) {
                crc.update(data, 0, length);
            }
            return crc.getValue();
        }
    }
}
