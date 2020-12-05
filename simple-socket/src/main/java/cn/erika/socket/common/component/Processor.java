package cn.erika.socket.common.component;

import cn.erika.config.Constant;
import cn.erika.config.GlobalSettings;
import cn.erika.util.compress.CompressException;
import cn.erika.util.compress.GZIP;
import cn.erika.util.security.RSA;
import cn.erika.util.security.Security;
import cn.erika.util.security.SecurityException;
import com.alibaba.fastjson.JSON;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Date;

public class Processor {
    private static final Charset CHARSET = Charset.forName("UTF-8");

    public static DataInfo beforeSend(byte[] data) throws IOException {
        try {
            DataInfo info = new DataInfo();
            info.setTimestamp(new Date());
            if (GlobalSettings.enableCompress) {
                switch (GlobalSettings.compressMethod) {
                    case NONE:
                        info.setCompress(DataInfo.Compress.NONE);
                        break;
                    case GZIP:
                        info.setCompress(DataInfo.Compress.GZIP);
                        data = GZIP.compress(data);
                        break;
                    default:
                        throw new CompressException("不支持的压缩格式");
                }
            }
            info.setPos(0);
            info.setLen(data.length);
            info.setData(data);
            return info;
        } catch (CompressException e) {
            throw new IOException(e.getMessage(), e);
        }
    }

    public static DataInfo beforeSend(BaseSocket socket, Message message) throws SecurityException, IOException {
        boolean isEncrypt = socket.get(Constant.ENCRYPT);
        if (isEncrypt) {
            message.setSign(RSA.sign(message.toString().getBytes(CHARSET), GlobalSettings.privateKey));
        }
        byte[] data = JSON.toJSONBytes(message);
        if (isEncrypt) {
            String password = socket.get(Constant.ENCRYPT_CODE);
            Security.Type passwordType = socket.get(Constant.ENCRYPT_TYPE);
            data = Security.encrypt(data, passwordType, password);
        }
        return beforeSend(data);
    }

    public static Message beforeRead(BaseSocket socket, DataInfo info, byte[] data) throws SecurityException, IOException {
        try {
            switch (info.getCompress()) {
                case NONE:
                    break;
                case GZIP:
                    data = GZIP.uncompress(data);
                    break;
                default:
                    throw new CompressException("不支持的压缩格式");
            }
            boolean isEncrypt = socket.get(Constant.ENCRYPT);
            if (isEncrypt) {
                String password = socket.get(Constant.ENCRYPT_CODE);
                Security.Type passwordType = socket.get(Constant.ENCRYPT_TYPE);
                data = Security.decrypt(data, passwordType, password);
            }
            Message message = JSON.parseObject(new String(data, CHARSET), Message.class);
            if (isEncrypt) {
                byte[] publicKey = socket.get(Constant.PUBLIC_KEY);
                if (!RSA.verify(message.toString().getBytes(CHARSET), publicKey, message.getSign())) {
                    throw new SecurityException("验签失败");
                }
            }
            return message;
        } catch (CompressException e) {
            throw new IOException(e.getMessage(), e);
        }
    }
}
