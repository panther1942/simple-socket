package cn.erika.socket.bio.handler;

import cn.erika.socket.Constant;
import cn.erika.socket.bio.core.DataInfo;
import cn.erika.socket.bio.core.Handler;
import cn.erika.socket.bio.core.TcpSocket;
import cn.erika.socket.bio.service.ISocketService;
import cn.erika.socket.bio.service.NotFoundServiceException;
import cn.erika.socket.bio.service.impl.EncryptResultService;
import cn.erika.socket.bio.service.impl.EncryptService;
import cn.erika.socket.bio.service.impl.PublicKeyService;
import cn.erika.socket.bio.service.impl.TextService;
import cn.erika.util.security.RSA;
import cn.erika.util.security.Security;
import cn.erika.util.security.SecurityException;
import com.alibaba.fastjson.JSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.SocketException;
import java.nio.charset.Charset;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

public abstract class AbstractHandler implements Handler {
    protected Logger log = LoggerFactory.getLogger(this.getClass());
    public static final Charset CHARSET = Charset.forName("UTF-8");
    private static Map<String, ISocketService> serviceList = new HashMap<>();

    private static byte[] publicKey;
    private static byte[] privateKey;

    static {
        register(Constant.SEVR_PUBLIC_KEY, new PublicKeyService());
        register(Constant.SEVR_EXCHANGE_KEY, new EncryptService());
        register(Constant.SEVR_ENCRYPT_RESULT, new EncryptResultService());
        register(Constant.SEVR_TEXT, new TextService());

        try {
            byte[][] keyPair = RSA.initKey(2048);
            publicKey = keyPair[0];
            privateKey = keyPair[1];
        } catch (NoSuchAlgorithmException e) {
            System.err.println(e.getMessage());
        }
    }

    public static byte[] getPublicKey() {
        return publicKey;
    }

    public static byte[] getPrivateKey() {
        return privateKey;
    }

    public static void register(String serviceName, ISocketService service) {
        serviceList.put(serviceName, service);
    }

    public static ISocketService getService(String serviceName) throws NotFoundServiceException {
        ISocketService service = serviceList.get(serviceName);
        if (service == null) {
            throw new NotFoundServiceException("不存在的服务: " + serviceName);
        }
        return service;
    }

    @Override
    public void init(TcpSocket socket) {
        socket.set(Constant.ENCRYPT, false);
    }

    @Override
    public void onMessage(TcpSocket socket, byte[] data, DataInfo info) {
        boolean isEncrypt = socket.get(Constant.ENCRYPT);
        try {
            if (isEncrypt) {
                String password = socket.get(Constant.PASSWORD);
                Security.Type passwordType = socket.get(Constant.PASSWORD_TYPE);
                data = Security.decrypt(data, passwordType, password);
            }
//            log.debug("Receive: " + new String(data, CHARSET));
            Message message = JSON.parseObject(new String(data, CHARSET), Message.class);
            if (isEncrypt) {
                byte[] publicKey = socket.get(Constant.PUBLIC_KEY);
                if (!RSA.verify(message.toString().getBytes(CHARSET), publicKey, message.getSign())) {
//                    log.debug("收到签名信息: " + Base64.getEncoder().encodeToString(message.getSign()));
                    throw new SecurityException("验签失败");
                }
            }
//            log.debug("收到消息: " + new String(message.getPayload(), CHARSET));
            if (!"exit".equalsIgnoreCase(message.getHead(Message.Head.Order))) {
                deal(socket, message);
            } else {
                onClose(socket);
            }
        } catch (SecurityException e) {
            log.error(e.getMessage(), e);
        }
    }

    public void sendMessage(TcpSocket socket, Message message) {
//        log.debug("发送消息: " + new String(JSON.toJSONBytes(message), CHARSET));
        boolean isEncrypt = socket.get(Constant.ENCRYPT);
        try {
            if (isEncrypt) {
                message.setSign(RSA.sign(message.toString().getBytes(CHARSET), privateKey));
//                log.debug("发送签名信息:" + Base64.getEncoder().encodeToString(message.getSign()));
            }
            byte[] data = JSON.toJSONBytes(message);
            if (isEncrypt) {
                String password = socket.get(Constant.PASSWORD);
                Security.Type passwordType = socket.get(Constant.PASSWORD_TYPE);
                data = Security.encrypt(data, passwordType, password);
            }
            socket.send(data);
        } catch (SocketException e) {
            log.debug("连接中断");
        } catch (IOException e) {
            onError(e.getMessage(), e);
        } catch (SecurityException e) {
            onError(e.getMessage(), e);
        }
    }

    public abstract void deal(TcpSocket socket, Message message);

    public abstract void close(TcpSocket socket);
}
