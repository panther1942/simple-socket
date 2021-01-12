package cn.erika.socket.core;

import cn.erika.config.Constant;
import cn.erika.config.GlobalSettings;
import cn.erika.context.exception.BeanException;
import cn.erika.socket.exception.DataException;
import cn.erika.socket.model.pto.DataInfo;
import cn.erika.socket.model.pto.Message;
import cn.erika.utils.exception.UnsupportedAlgorithmException;
import cn.erika.utils.io.compress.CompressUtils;
import cn.erika.utils.exception.CompressException;
import cn.erika.utils.exception.NoSuchCompressAlgorithm;
import cn.erika.utils.exception.SerialException;
import cn.erika.utils.log.Logger;
import cn.erika.utils.log.LoggerFactory;
import cn.erika.utils.security.DigitalSignatureAlgorithm;
import cn.erika.utils.security.MessageDigestUtils;
import cn.erika.utils.security.SecurityAlgorithm;
import cn.erika.utils.security.SecurityUtils;
import cn.erika.utils.security.algorithm.BasicMessageDigestAlgorithm;
import cn.erika.utils.io.SerialUtils;
import cn.erika.utils.string.StringUtils;

import java.io.IOException;
import java.net.SocketException;
import java.nio.charset.Charset;
import java.security.InvalidKeyException;
import java.security.SignatureException;
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
    public synchronized void send(Message message) {
        // 先刷新连接最后通信时间
        Date now = new Date();
        set(Constant.LAST_TIME, now);
        try {
            boolean isEncrypt = get(Constant.ENCRYPT);
            // 因为可能复用Message 所以需要先去除上次的签名信息
            message.del(Constant.DIGITAL_SIGNATURE);
            // 如果加密通信已启用
            if (isEncrypt) {
                // 根据协商的签名进行数字签名
                DigitalSignatureAlgorithm digitalSignatureAlgorithm = get(Constant.DIGITAL_SIGNATURE_ALGORITHM);
                byte[] rsaSign = SecurityUtils.sign(
                        message.toString().getBytes(charset),
                        GlobalSettings.privateKey,
                        digitalSignatureAlgorithm);
                message.add(Constant.DIGITAL_SIGNATURE, rsaSign);
            }
            // 序列化Message
            byte[] data = SerialUtils.serialObject(message);
            // 如果加密通信已启用
            if (isEncrypt) {
                // 根据协商的加密算法进行数据加密
                SecurityAlgorithm securityAlgorithm = get(Constant.SECURITY_ALGORITHM);
                String securityKey = get(Constant.SECURITY_KEY);
                byte[] securityIv = get(Constant.SECURITY_IV); // 向量会根据算法需要使用或者忽略
                data = SecurityUtils.encrypt(data, securityKey, securityAlgorithm, securityIv);
            }
            // 创建数据包
            DataInfo info = new DataInfo();
            info.setTimestamp(now);
            if (GlobalSettings.enableCompress) {
                int compressCode = GlobalSettings.compressCode;
                info.setCompress(compressCode);
                data = CompressUtils.compress(data, compressCode);
            }
            info.setPos(0);
            info.setLen(data.length);
            info.setData(data);
            send(info);
        } catch (CompressException | NoSuchCompressAlgorithm e) {
            log.error("压缩时出现错误: " + e.getMessage(), e);
            close();
        } catch (SerialException e) {
            log.error("序列化出现错误: " + e.getMessage(), e);
            close();
        } catch (InvalidKeyException e) {
            log.error("公钥无效");
            close();
        } catch (UnsupportedAlgorithmException e) {
            log.error(e.getMessage(), e);
            close();
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
    public synchronized void receive(DataInfo info) {
        set(Constant.LAST_TIME, new Date());
        try {
            boolean isEncrypt = get(Constant.ENCRYPT);
            byte[] data = info.getData();
            int compressCode = info.getCompress();
            data = CompressUtils.uncompress(data, compressCode);
            if (isEncrypt) {
                SecurityAlgorithm securityAlgorithm = get(Constant.SECURITY_ALGORITHM);
                String securityKey = get(Constant.SECURITY_KEY);
                byte[] securityIv = get(Constant.SECURITY_IV);
                data = SecurityUtils.decrypt(data, securityKey, securityAlgorithm, securityIv);
            }
            Message message = SerialUtils.serialObject(data, Message.class);
            if (message == null) {
                System.err.printf("[%d] Message is null", info.getTimestamp().getTime());
                throw new DataException("数据为空");
            }
            if (isEncrypt) {
                byte[] publicKey = get(Constant.PUBLIC_KEY);
                DigitalSignatureAlgorithm digitalSignatureAlgorithm = get(Constant.DIGITAL_SIGNATURE_ALGORITHM);
                byte[] rsaSign = message.get(Constant.DIGITAL_SIGNATURE);
                message.del(Constant.DIGITAL_SIGNATURE);
                if (!SecurityUtils.verify(message.toString().getBytes(charset),
                        rsaSign, publicKey, digitalSignatureAlgorithm)) {
                    throw new InvalidKeyException("验签失败");
                }
                message.add(Constant.DIGITAL_SIGNATURE, rsaSign);
            }
            receive(message);
        } catch (CompressException | NoSuchCompressAlgorithm e) {
            log.error("解压缩时出现错误: " + e.getMessage(), e);
        } catch (SerialException e) {
            log.error("反序列化出现错误: " + e.getMessage(), e);
        } catch (InvalidKeyException e) {
            log.error("签名验证失败", e);
            close();
        } catch (UnsupportedAlgorithmException | DataException e) {
            log.error(e.getMessage(), e);
            close();
        }
    }

    /**
     * 发送消息要分两次发送 先发送消息头 在发送消息体
     *
     * @param info 要发送的数据包 要经过send(Message)包装
     */
    private synchronized void send(DataInfo info) {
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
    private synchronized void receive(Message message) {
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
        if (k != null) {
            return (T) this.attr.remove(k);
        } else {
            return null;
        }
    }

    @Override
    public Handler getHandler() {
        return this.handler;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ISocket)) {
            return false;
        }
        ISocket that = (ISocket) o;
        if (this.getLocalAddress().equals(that.getLocalAddress())
                && this.getRemoteAddress().equals(that.getRemoteAddress())) {
            return true;
        }
        return false;
    }
}
