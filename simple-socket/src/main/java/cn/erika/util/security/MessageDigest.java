package cn.erika.util.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.security.NoSuchAlgorithmException;

public class MessageDigest {
    private static Logger log = LoggerFactory.getLogger(MessageDigest.class);

    public static String sum(String data, Type algorithm) throws SecurityException {
        return sum(data, Charset.forName("UTF-8"), algorithm);
    }

    public static String sum(String data, Charset charset, Type algorithm) throws SecurityException {
        return sum(data.getBytes(charset), algorithm);
    }

    public static String sum(File file, Type algorithm) throws SecurityException, IOException {
        try {
            java.security.MessageDigest digest = java.security.MessageDigest.getInstance(algorithm.getName());
            try (FileInputStream in = new FileInputStream(file)) {
                int length = 0;
                byte[] data = new byte[4096];

                while ((length = in.read(data)) != -1) {
                    digest.update(data, 0, length);
                }
                byte[] result = digest.digest();
                return byteToHexString(result);
            }
        } catch (NoSuchAlgorithmException e) {
            throw new SecurityException("当前系统不支持该算法: " + algorithm, e);
        }
    }

    public static String sum(byte[] data, Type algorithm) throws SecurityException {
        if (data == null) {
            throw new IllegalArgumentException("不能对空值签名");
        }
        try {
            java.security.MessageDigest digest = java.security.MessageDigest.getInstance(algorithm.getName());
            digest.update(data);
            byte[] result = digest.digest();
            return byteToHexString(result);
        } catch (NoSuchAlgorithmException e) {
            throw new SecurityException("当前系统不支持该算法: " + algorithm, e);
        }
    }

    private static String byteToHexString(byte[] data) {
        StringBuilder buffer = new StringBuilder();
        for (byte b : data) {
            int i = b;
            if (i < 0)
                i += 256;
            if (i < 16)
                buffer.append(0);
            buffer.append(Integer.toHexString(i));
        }
        return buffer.toString();
    }

    public enum Type {
        MD5("MD5"),
        SHA1("SHA-1"),
        SHA256("SHA-256"),
        SHA384("SHA-384"),
        SHA512("SHA-512");

        private String name;

        Type(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }
}
