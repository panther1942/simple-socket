package cn.erika.socket.core;

import cn.erika.config.Constant;
import cn.erika.config.GlobalSettings;
import cn.erika.context.exception.BeanException;
import cn.erika.socket.core.component.DataInfo;
import cn.erika.socket.core.component.Message;
import cn.erika.util.compress.GZIP;
import cn.erika.util.exception.CompressException;
import cn.erika.util.exception.SerialException;
import cn.erika.util.log.Logger;
import cn.erika.util.log.LoggerFactory;
import cn.erika.util.security.DigitalSignatureAlgorithm;
import cn.erika.util.security.SecurityAlgorithm;
import cn.erika.util.security.SecurityUtils;
import cn.erika.util.string.SerialUtils;

import java.io.IOException;
import java.net.SocketException;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public abstract class BaseSocket implements ISocket {
    // 记录连接的属性
    private Map<String, Object> attr = new HashMap<>();
    protected Logger log = LoggerFactory.getLogger(this.getClass());
    protected Handler handler;
    protected Charset charset;

    /**
     * 发送消息的方法 能够被上层调用 执行逻辑为
     * <p>
     * 1、计算签名 并将计算结果加到Message的payload属性里
     * 2、序列化 Message -> byte[]
     * 3、加密 byte[] -> byte[]
     * 4、压缩 byte[] -> byte[]
     * 5、组装成DataInfo 调用send(DataInfo)发送
     *
     * @param message 需要发送的消息需要包裹在Message对象中以识别服务名称
     */
    @Override
    public void send(Message message) {
        try {
            boolean isEncrypt = get(Constant.ENCRYPT);
            message.del(Constant.DIGITAL_SIGNATURE);
            if (isEncrypt) {
                DigitalSignatureAlgorithm digitalSignatureAlgorithm = get(Constant.DIGITAL_SIGNATURE_ALGORITHM);
                message.add(Constant.DIGITAL_SIGNATURE, SecurityUtils.sign(
                        SerialUtils.serialObject(message),
                        GlobalSettings.privateKey,
                        digitalSignatureAlgorithm)
                );
            }
            byte[] data = SerialUtils.serialObject(message);
            if (isEncrypt) {
                SecurityAlgorithm securityAlgorithm = get(Constant.SECURITY_ALGORITHM);
                String securityKey = get(Constant.SECURITY_KEY);
                byte[] securityIv = get(Constant.SECURITY_IV);
                data = SecurityUtils.encrypt(data, securityKey, securityAlgorithm, securityIv);
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
        } catch (CompressException e) {
            log.error("压缩时出现错误: " + e.getMessage());
        } catch (SerialException e) {
            log.error("序列化出现错误: " + e.getMessage());
        }
    }


    /**
     * 接收消息的方法 被Reader调用 执行逻辑为
     * <p>
     * 1、解压缩 byte[] -> byte[]
     * 2、解密 byte[] -> byte[]
     * 3、反序列化 byte[] -> Message
     * 4、验证签名
     * 5、处理Message
     *
     * @param info 接收到的数据包 将解析成Message对象
     */
    public void receive(DataInfo info) {
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
                SecurityAlgorithm securityAlgorithm = get(Constant.SECURITY_ALGORITHM);
                String securityKey = get(Constant.SECURITY_KEY);
                byte[] securityIv = get(Constant.SECURITY_IV);
                data = SecurityUtils.decrypt(data, securityKey, securityAlgorithm, securityIv);
            }
            Message message = SerialUtils.serialObject(data);
            if (isEncrypt) {
                byte[] publicKey = get(Constant.PUBLIC_KEY);
                DigitalSignatureAlgorithm digitalSignatureAlgorithm = get(Constant.DIGITAL_SIGNATURE_ALGORITHM);
                byte[] sign = message.get(Constant.DIGITAL_SIGNATURE);
                message.del(Constant.DIGITAL_SIGNATURE);
                if (!SecurityUtils.verify(SerialUtils.serialObject(message),
                        sign, publicKey, digitalSignatureAlgorithm)) {
                    throw new SecurityException("验签失败");
                }
                message.add(Constant.DIGITAL_SIGNATURE, sign);
            }
            receive(message);
        } catch (CompressException e) {
            log.error("解压缩时出现错误: " + e.getMessage());
        } catch (SerialException e) {
            log.error("反序列化出现错误: " + e.getMessage());
        } catch (SecurityException e) {
            log.error("签名验证失败", e);
            close();
        }
    }

    /**
     * 发送消息要分两次发送 先发送消息头 在发送消息体
     *
     * @param info 要发送的数据包 要经过send(Message)包装
     */
    private void send(DataInfo info) {
        try {
            send(info.toString().getBytes(charset));
            send(info.getData());
        } catch (SocketException e) {
            log.warn("连接断开");
            close();
        } catch (IOException e) {
            handler.onError(this, e);
        }
    }

    /**
     * 接收完消息后需要交给Handler处理 此处添加此方法是为了方便拦截处理
     *
     * @param message 收到的消息内容 已经解压缩和解密
     */
    private void receive(Message message) {
        try {
            handler.onMessage(this, message);
        } catch (BeanException e) {
            handler.onError(this, e);
        }
    }

    /**
     * 实际发送消息的方法 需要底层实现
     *
     * @param data 要发送的数据内容 长度即为数组长度
     * @throws IOException 如果发送数据时出现错误 则向外抛出
     */
    public abstract void send(byte[] data) throws IOException;

    // 设置连接额外属性
    @SuppressWarnings("unchecked")
    @Override
    public <T> T set(String k, Object v) {
        return (T) this.attr.put(k, v);
    }

    // 获取连接额外属性
    @SuppressWarnings("unchecked")
    @Override
    public <T> T get(String k) {
        return (T) this.attr.get(k);
    }

    // 移除连接额外属性
    @SuppressWarnings("unchecked")
    @Override
    public <T> T remove(String k) {
        return (T) this.attr.remove(k);
    }

    @Override
    public Handler getHandler() {
        return this.handler;
    }
}
