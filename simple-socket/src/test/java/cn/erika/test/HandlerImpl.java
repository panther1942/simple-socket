package cn.erika.test;

import cn.erika.socket.core.Handler;
import cn.erika.socket.core.TcpSocket;
import cn.erika.util.security.MessageDigest;
import cn.erika.util.security.Security;
import cn.erika.util.security.SecurityException;
import com.alibaba.fastjson.JSON;

import java.io.IOException;
import java.nio.charset.Charset;

public abstract class HandlerImpl implements Handler {
    public static final Charset CHARSET = Charset.forName("UTF-8");
    private String password = "123456";

    @Override
    public void init(TcpSocket socket) {

    }

    @Override
    public void onMessage(TcpSocket socket, byte[] data) throws IOException {

        Message msg = JSON.parseObject(new String(
                Security.decrypt(data, Security.Type.AES128ECB, password), CHARSET),
                Message.class);
        String message = new String(msg.getBody(), CHARSET);
        if (!"exit".equalsIgnoreCase(message)) {
            System.out.println(message);
        } else {
            onClose(socket);
        }
    }

    public void sendMessage(TcpSocket socket, String message) {
        try {
            Message msg = new Message();
            msg.addHead(Message.Head.Sign, MessageDigest.Type.MD5);
            msg.setBody(message.getBytes(CHARSET));
            msg.setSign(MessageDigest.sum(msg.getBody(), MessageDigest.Type.MD5).getBytes(CHARSET));

            byte[] data = JSON.toJSONString(msg).getBytes(CHARSET);
            socket.send(Security.encrypt(data, Security.Type.AES128ECB, password));
        } catch (IOException e) {
            onError(e.getMessage(), e);
        } catch (SecurityException e) {
            onError(e.getMessage(), e);
        }
    }
}
