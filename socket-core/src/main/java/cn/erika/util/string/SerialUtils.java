package cn.erika.util.string;

import cn.erika.util.exception.SerialException;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.parser.ParserConfig;
import com.alibaba.fastjson.serializer.SerializerFeature;

public class SerialUtils {

    static{
        ParserConfig.getGlobalInstance().addAccept("cn.erika.");
    }

    public static byte[] serialObject(Object obj) throws SerialException {
        return JSON.toJSONBytes(obj, SerializerFeature.WriteClassName);
    }

    public static <T> T serialObject(byte[] data, Class<T> clazz) throws SerialException {
        return JSON.parseObject(data, clazz);
    }
}
