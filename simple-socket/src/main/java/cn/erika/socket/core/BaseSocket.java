package cn.erika.socket.core;

import cn.erika.config.Constant;
import cn.erika.config.GlobalSettings;
import cn.erika.socket.component.Message;
import cn.erika.util.compress.CompressException;
import cn.erika.util.compress.GZIP;
import cn.erika.util.security.RSA;
import cn.erika.util.security.Security;
import cn.erika.util.security.SecurityException;
import cn.erika.util.string.SerialUtils;

import java.io.IOException;
import java.net.SocketAddress;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 该类用于屏蔽底层不同 使得处理器能以相同的方式处理BIO和NIO请求
 */
public abstract class BaseSocket {
    // 记录连接的属性
    private Map<String, Object> attr = new HashMap<>();

    /**
     * 发送消息的方法
     *
     * @param message 发送的消息必须包装在Message对象中
     *                其中head中存放Head.ServerName为服务名称（标识）
     *                也可存放其他内容 目前使用数据头和数据体分开的方式 方便扩展
     */
    public abstract void send(Message message);

    /**
     * TcpReader解析数据完成后包装成DataInfo 目的是确保数据完整 而且防止粘包
     *
     * @param info 解析完整的数据
     */
    public abstract void receive(DataInfo info);

    /**
     * 为避免服务类之间的直接互相调用引起程序结构混乱（App.getBean我觉得都已经够乱了）
     * 当身份鉴定程序完成认定后（token）执行handler.ready()方法
     * 目前是为了发送文件实现的自动化身份验证
     */
    public abstract void ready();

    /**
     * 获取远端地址
     * Socket和SocketChannel的底层方法不同 故用此方法屏蔽差异
     *
     * @return 远端地址
     * @throws IOException 如果SocketChannel抛出异常 则直接把他的异常抛出来
     */
    public abstract SocketAddress getRemoteAddress() throws IOException;

    /**
     * 检查连接是否关闭
     * Socket和SocketChannel的底层方法不同 故用此方法屏蔽差异
     *
     * @return True 连接关闭或未启动 False 连接正常
     */
    public abstract boolean isClosed();

    /**
     * 关闭Socket连接
     */
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

    /**
     * 发送前要进行数据压缩,数据的加密以及数据的包装
     *
     * @param socket  要发送数据的BaseSocket对象
     * @param message 要发送的消息
     * @return 封装好的数据包
     * @throws SecurityException 如果加密过程出现错误则抛出该异常
     * @throws IOException       如果序列化过程或者压缩过程出现错误则抛出该异常
     */
    DataInfo beforeSend(BaseSocket socket, Message message) throws SecurityException, IOException {
        // 因为可能直接拿上次用过的message，所以需要先去掉签名
        message.setSign(null);
        boolean isEncrypt = socket.get(Constant.ENCRYPT);
        if (isEncrypt) {
            String rsaAlgorithm = socket.get(Constant.RSA_ALGORITHM);
            message.setSign(RSA.sign(SerialUtils.serialObject(message), GlobalSettings.privateKey, rsaAlgorithm));
        }
        byte[] data = SerialUtils.serialObject(message);
        if (isEncrypt) {
            String password = socket.get(Constant.SECURITY_CODE);
            Security.Type passwordType = socket.get(Constant.SECURITY_NAME);
            data = Security.encrypt(data, passwordType, password);
        }
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

    /**
     * 读取数据前需要解密解压缩等操作
     *
     * @param socket 要发送的消息
     * @param info   解析完整的数据包
     * @return 处理后的数据
     * @throws SecurityException 如果解密过程出现错误则抛出该异常
     * @throws IOException       如果反序列化过程或者解压缩过程出现错误则抛出该异常
     */
    Message beforeRead(BaseSocket socket, DataInfo info) throws SecurityException, IOException {
        byte[] data = info.getData();
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
        } catch (CompressException e) {
            throw new IOException(e.getMessage(), e);
        }
        boolean isEncrypt = socket.get(Constant.ENCRYPT);
        if (isEncrypt) {
            String password = socket.get(Constant.SECURITY_CODE);
            Security.Type passwordType = socket.get(Constant.SECURITY_NAME);
            data = Security.decrypt(data, passwordType, password);
        }
        Message message = SerialUtils.serialObject(data);
        if (isEncrypt) {
            byte[] publicKey = socket.get(Constant.PUBLIC_KEY);
            String rsaAlgorithm = socket.get(Constant.RSA_ALGORITHM);
            byte[] sign = message.getSign();
            message.setSign(null);
            if (!RSA.verify(SerialUtils.serialObject(message), publicKey, sign, rsaAlgorithm)) {
                throw new SecurityException("验签失败");
            }
            message.setSign(sign);
        }
        return message;
    }
}
