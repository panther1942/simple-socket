package cn.erika.utils.security;

import cn.erika.utils.exception.UnsupportedAlgorithmException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.zip.CRC32;

/**
 * 消息签名工具类
 */
public class MessageDigestUtils {

    /**
     * 使用指定的算法计算校验和 编码将使用UTF-8计算 实际调用sum(byte[], BasicMessageDigestAlgorithm)方法
     *
     * @param data      要计算校验和的字符串
     * @param algorithm 要使用的签名算法
     * @return 签名的校验和
     * @throws IllegalArgumentException      如果要签名的数据为空值 则抛出该异常
     * @throws UnsupportedAlgorithmException 如果当前平台不支持指定的算法则抛出该错误信息]
     */
    public static byte[] sum(String data,
                             MessageDigestAlgorithm algorithm)
            throws IllegalArgumentException, UnsupportedAlgorithmException {
        return sum(data, Charset.forName("UTF-8"), algorithm);
    }

    /**
     * 使用指定的算法和字符集计算校验和 实际调用sum(byte[], BasicMessageDigestAlgorithm)方法
     *
     * @param data      要计算校验和的字符串
     * @param charset   指定的字符集
     * @param algorithm 要使用的签名算法
     * @return 签名的校验和
     * @throws IllegalArgumentException      如果要签名的数据为空值 则抛出该异常
     * @throws UnsupportedAlgorithmException 如果当前平台不支持指定的算法则抛出该错误信息]
     */
    public static byte[] sum(String data,
                             Charset charset,
                             MessageDigestAlgorithm algorithm)
            throws IllegalArgumentException, UnsupportedAlgorithmException {
        return sum(data.getBytes(charset), algorithm);
    }

    /**
     * 使用指定的算法计算校验和
     *
     * @param data      要计算校验和的数据
     * @param algorithm 要使用的签名算法
     * @return 签名的校验和
     * @throws IllegalArgumentException      如果要签名的数据为空值 则抛出该异常
     * @throws UnsupportedAlgorithmException 如果当前平台不支持指定的算法则抛出该错误信息]
     */
    public static byte[] sum(byte[] data,
                             MessageDigestAlgorithm algorithm)
            throws IllegalArgumentException, UnsupportedAlgorithmException {
        if (data == null) {
            throw new IllegalArgumentException("不能对空值签名");
        }
        try {
            MessageDigest digest = MessageDigest.getInstance(algorithm.getValue());
            digest.update(data);
            return digest.digest();
        } catch (NoSuchAlgorithmException e) {
            throw new UnsupportedAlgorithmException("当前系统不支持该算法: " + algorithm, e);
        }
    }

    /**
     * 使用制定的算法计算文件的签名校验和
     *
     * @param file      要计算签名的文件
     * @param algorithm 要使用的签名算法
     * @return 签名的校验和
     * @throws IOException                   如果读文件的过程发生错误 则抛出该异常信息
     * @throws UnsupportedAlgorithmException 如果当前平台不支持指定的算法则抛出该错误信息
     */
    public static byte[] sum(File file, MessageDigestAlgorithm algorithm)
            throws IOException, UnsupportedAlgorithmException {
        try {
            MessageDigest digest = MessageDigest.getInstance(algorithm.getValue());
            try (FileInputStream in = new FileInputStream(file)) {
                int length = 0;
                byte[] data = new byte[4 * 1024];

                while ((length = in.read(data)) != -1) {
                    digest.update(data, 0, length);
                }
                return digest.digest();
            }
        } catch (NoSuchAlgorithmException e) {
            throw new UnsupportedAlgorithmException("当前系统不支持该算法: " + algorithm, e);
        }
    }

    /**
     * CRC32循环冗余校验
     *
     * @param data 要计算校验和的数据
     * @return 计算的校验和
     */
    public static long crc32Sum(byte[] data) {
        CRC32 crc = new CRC32();
        crc.update(data);
        return crc.getValue();
    }

    /**
     * CRC32循环冗余校验
     * 建议优先用该算法签名文件信息(其他算法过于复杂 对CPU算力要求高)
     *
     * @param file 要计算校验和的文件
     * @return 计算的校验和
     * @throws IOException 如果读取文件的过程中出现错误
     */
    public static long crc32Sum(File file) throws IOException {
        try (RandomAccessFile in = new RandomAccessFile(file, "r")) {
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
