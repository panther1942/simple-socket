package cn.erika.util.security;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.security.NoSuchAlgorithmException;

public class MessageDigest {

    public static byte[] sum(String data, Type algorithm) throws SecurityException {
        return sum(data, Charset.forName("UTF-8"), algorithm);
    }

    public static byte[] sum(String data, Charset charset, Type algorithm) throws SecurityException {
        return sum(data.getBytes(charset), algorithm);
    }

    public static byte[] sum(File file, Type algorithm) throws SecurityException, IOException {
        try {
            java.security.MessageDigest digest = java.security.MessageDigest.getInstance(algorithm.value);
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

    public static byte[] sum(byte[] data, Type algorithm) throws SecurityException {
        if (data == null) {
            throw new IllegalArgumentException("不能对空值签名");
        }
        try {
            java.security.MessageDigest digest = java.security.MessageDigest.getInstance(algorithm.value);
            digest.update(data);
            return digest.digest();
        } catch (NoSuchAlgorithmException e) {
            throw new SecurityException("当前系统不支持该算法: " + algorithm, e);
        }
    }

    public static String byteToHexString(byte[] data) {
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

        private String value;

        Type(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public static Type getByName(String name) {
            for (Type type : Type.values()) {
                if (type.value.equals(name)) {
                    return type;
                }
            }
            return null;
        }
    }
}
