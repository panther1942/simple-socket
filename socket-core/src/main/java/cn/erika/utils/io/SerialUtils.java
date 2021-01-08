package cn.erika.utils.io;

import cn.erika.utils.exception.SerialException;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.parser.ParserConfig;
import com.alibaba.fastjson.serializer.SerializerFeature;

import java.io.*;

public class SerialUtils {

    static {
        ParserConfig.getGlobalInstance().addAccept("cn.erika.");
    }

    /**
     * 序列化对象为字节数组
     * 此处用fastjson将java对象序列化为json格式的字节数组
     *
     * @param obj 要序列化的对象
     * @return 序列化的字节数组
     * @throws SerialException 不知道 先写上 万一用得到
     */
    public static byte[] serialObject(Object obj) throws SerialException {
        return JSON.toJSONBytes(obj,
                SerializerFeature.WriteClassName,
                SerializerFeature.WriteMapNullValue);
    }

    /**
     * 反序列化对象为Java对象
     * 此处用fastjson将json字节数组反序列化为java格式
     *
     * @param data  json字节数组
     * @param clazz 要反序列化的类
     * @param <T>   不知道 就当自动强转了
     * @return 反序列化的对象
     * @throws SerialException 不知道 先写上 万一用得到
     */
    public static <T> T serialObject(byte[] data, Class<T> clazz) throws SerialException {
        T object = JSON.parseObject(data, clazz);
        if (object == null) {
            throw new SerialException("反序列化异常: " + new String(data));
        }
        return object;
    }

    /**
     * jdk提供的序列化数据的方式
     *
     * @param obj 要序列化的对象
     * @return 序列化得到的字节数组
     * @throws SerialException 如果序列化过程出现错误 则抛出该异常
     */
    public static byte[] serialJavaObject(Object obj) throws SerialException {
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

    /**
     * jdk提供的反序列化数据的方式 在socket测试中出现问题 正在排查
     *
     * @param data 要反序列化的字节数组
     * @return 反序列化得到的java对象
     * @throws SerialException 如果序列化过程出现错误 则抛出该异常
     */
    @SuppressWarnings("unchecked")
    public static <T> T serialJavaObject(byte[] data) throws SerialException {
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
