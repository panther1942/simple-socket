package cn.erika.test.socket.handler;

import cn.erika.socket.core.Handler;
import cn.erika.socket.core.TcpSocket;
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

public abstract class AbstractHandler implements Handler {
    protected Logger log = LoggerFactory.getLogger(this.getClass());
    public static final Charset CHARSET = Charset.forName("UTF-8");

    protected static byte[] publicKey;
    protected static byte[] privateKey;

    static {
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

    @Override
    public void init(TcpSocket socket) {
        socket.set(DefineString.ENCRYPT, false);
    }

    @Override
    public void onMessage(TcpSocket socket, byte[] data) {
        boolean isEncrypt = socket.get(DefineString.ENCRYPT);
        try {
            if (isEncrypt) {
                String password = socket.get(DefineString.PASSWORD);
                Security.Type passwordType = socket.get(DefineString.PASSWORD_TYPE);
                data = Security.decrypt(data, passwordType, password);
            }
            Message message = JSON.parseObject(new String(data, CHARSET), Message.class);
            if (isEncrypt) {
                byte[] publicKey = socket.get(DefineString.PUBLIC_KEY);
                if (!RSA.verify(message.toString().getBytes(CHARSET), publicKey, message.getSign())) {
//                    log.debug("收到签名信息: " + Base64.getEncoder().encodeToString(message.getSign()));
                    throw new SecurityException("验签失败");
                }
            }
//            log.debug("收到消息: " + new String(message.getPayload(), CHARSET));
            if (!"exit".equalsIgnoreCase(message.getHead(Message.Head.REQUEST))) {
                deal(socket, message);
            } else {
                onClose(socket);
            }
        } catch (SecurityException e) {
            log.error(e.getMessage(), e);
        }
    }

    public void sendMessage(TcpSocket socket, Message message) {
//        log.debug("发送消息: " + new String(message.getPayload(), CHARSET));
        boolean isEncrypt = socket.get(DefineString.ENCRYPT);
        try {
            if (isEncrypt) {
                message.setSign(RSA.sign(message.toString().getBytes(CHARSET), privateKey));
//                log.debug("发送签名信息:" + Base64.getEncoder().encodeToString(message.getSign()));
            }
            byte[] data = JSON.toJSONBytes(message);
            if (isEncrypt) {
                String password = socket.get(DefineString.PASSWORD);
                Security.Type passwordType = socket.get(DefineString.PASSWORD_TYPE);
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
