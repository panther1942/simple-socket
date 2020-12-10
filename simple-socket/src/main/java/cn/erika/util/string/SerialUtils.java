package cn.erika.util.string;

import java.io.*;

public class SerialUtils {
    public static byte[] serialObject(Object obj) throws IOException {
        ByteArrayOutputStream bOut = null;
        ObjectOutputStream out = null;
        try {
            bOut = new ByteArrayOutputStream();
            out = new ObjectOutputStream(bOut);
            out.writeObject(obj);
            return bOut.toByteArray();
        } finally {
            if (out != null) {
                out.close();
            }
            if (bOut != null) {
                bOut.close();
            }
        }
    }

    public static <T> T serialObject(byte[] data) throws IOException {
        ByteArrayInputStream bIn = null;
        ObjectInputStream in = null;
        try {
            bIn = new ByteArrayInputStream(data);
            in = new ObjectInputStream(bIn);
            return (T) in.readObject();
        } catch (ClassNotFoundException e) {
            throw new IOException("找不到实现反序列化的类");
        }finally {
            if (in != null) {
                in.close();
            }
            if (bIn != null) {
                bIn.close();
            }
        }
    }
}
