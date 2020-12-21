package cn.erika.socket.core;

import cn.erika.socket.config.Constant;
import cn.erika.socket.config.GlobalSettings;
import cn.erika.socket.core.component.DataInfo;
import cn.erika.socket.core.component.Message;
import cn.erika.util.compress.GZIP;
import cn.erika.util.exception.CompressException;
import cn.erika.util.exception.SecurityException;
import cn.erika.util.security.RSA;
import cn.erika.util.security.Security;
import cn.erika.util.string.SerialUtils;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public abstract class Socket {
    // 记录连接的属性
    private Map<String, Object> attr = new HashMap<>();

    // 1、计算签名 并将计算结果加到Message的payload属性里
    // 2、序列化 Message -> byte[]
    // 3、加密 byte[] -> byte[]
    // 4、压缩 byte[] -> byte[]
    // 5、组装成DataInfo 调用send(DataInfo)发送
    public void send(Message message) throws IOException {
        try {
            boolean isEncrypt = get(Constant.ENCRYPT);
            message.del(Constant.SIGN);
            if (isEncrypt) {
                String rsaAlgorithm = get(Constant.RSA_ALGORITHM);
                message.add(Constant.SIGN, RSA.sign(SerialUtils.serialObject(message), GlobalSettings.privateKey, rsaAlgorithm));
            }
            byte[] data = SerialUtils.serialObject(message);
            if (isEncrypt) {
                Security.Type securityAlgorithm = get(Constant.SECURITY_ALGORITHM);
                String securityKey = get(Constant.SECURITY_KEY);
                byte[] securityIv = get(Constant.SECURITY_IV);
                data = Security.encrypt(data, securityAlgorithm, securityKey, securityIv);
            }
            DataInfo info = new DataInfo();
            info.setTimestamp(new Date());
            if (GlobalSettings.enableCompress) {
                switch (GlobalSettings.compressType) {
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
            send(info);
        } catch (Exception e) {
            throw new IOException(e);
        }
    }

    // 1、解压缩 byte[] -> byte[]
    // 2、解密 byte[] -> byte[]
    // 3、反序列化 byte[] -> Message
    // 4、验证签名
    // 5、处理Message
    public void receive(DataInfo info) throws IOException {
        try {
            boolean isEncrypt = get(Constant.ENCRYPT);
            byte[] data = info.getData();
            switch (info.getCompress()) {
                case NONE:
                    break;
                case GZIP:
                    data = GZIP.uncompress(data);
                    break;
                default:
                    throw new CompressException("不支持的压缩格式");
            }
            if (isEncrypt) {
                Security.Type securityAlgorithm = get(Constant.SECURITY_ALGORITHM);
                String securityKey = get(Constant.SECURITY_KEY);
                byte[] securityIv = get(Constant.SECURITY_IV);
                data = Security.decrypt(data, securityAlgorithm, securityKey, securityIv);
            }
            Message message = SerialUtils.serialObject(data);
            if (isEncrypt) {
                byte[] publicKey = get(Constant.PUBLIC_KEY);
                String rsaAlgorithm = get(Constant.RSA_ALGORITHM);
                byte[] sign = message.get(Constant.SIGN);
                message.del(Constant.SIGN);
                if (!RSA.verify(SerialUtils.serialObject(message), publicKey, sign, rsaAlgorithm)) {
                    throw new SecurityException("验签失败");
                }
                message.add(Constant.SIGN, sign);
            }
            receive(message);
        } catch (Exception e) {
            throw new IOException(e);
        }
    }

    public abstract void send(DataInfo info);

    public abstract void receive(Message message);

    public abstract boolean isClosed();

    public abstract void close();

    // 设置连接额外属性
    @SuppressWarnings("unchecked")
    public <T> T set(String k, Object v) {
        return (T) this.attr.put(k, v);
    }

    // 获取连接额外属性
    @SuppressWarnings("unchecked")
    public <T> T get(String k) {
        return (T) this.attr.get(k);
    }

    // 移除连接额外属性
    @SuppressWarnings("unchecked")
    public <T> T remove(String k) {
        return (T) this.attr.remove(k);
    }
}
