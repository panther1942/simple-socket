package cn.erika.util;

import cn.erika.utils.exception.SerialException;

import java.io.*;

public class SerialUtils {
    public static byte[] serialObject(Object obj) throws SerialException {
        ByteArrayOutputStream bOut = null;
        ObjectOutputStream out = null;
        try {
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
        } catch (IOException e) {
            e.printStackTrace();
            throw new SerialException(e);
        }
    }

    public static <T> T serialObject(byte[] data) throws SerialException {
        ByteArrayInputStream bIn = null;
        ObjectInputStream in = null;
        try {
            try {
                bIn = new ByteArrayInputStream(data);
                // 错误发生在这一行
                in = new ObjectInputStream(bIn);
                return (T) in.readObject();
            } catch (ClassNotFoundException e) {
                throw new SerialException("找不到实现反序列化的类", e);
            } finally {
                if (in != null) {
                    in.close();
                }
                if (bIn != null) {
                    bIn.close();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new SerialException(e);
        }
    }
}
