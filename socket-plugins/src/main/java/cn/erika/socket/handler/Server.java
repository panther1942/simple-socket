package cn.erika.socket.handler;

import cn.erika.config.Constant;
import cn.erika.socket.core.BaseHandler;
import cn.erika.socket.core.Socket;
import cn.erika.socket.core.component.LinkManager;
import cn.erika.socket.core.component.Message;
import cn.erika.socket.exception.TokenException;
import cn.erika.util.log.Logger;
import cn.erika.util.log.LoggerFactory;

import java.io.IOException;
import java.net.SocketException;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class Server extends BaseHandler {
    private LinkManager linkManager = new LinkManager();
    private Map<String, Socket> tokenList = new ConcurrentHashMap<>();
    private Logger log = LoggerFactory.getLogger(this.getClass());

    @Override
    public void init(Socket socket) {
        super.init(socket);
        System.out.println("新连接接入");
        linkManager.addLink(socket);
    }

    @Override
    public void onClose(Socket socket) {
        linkManager.popLink(socket);
    }

    public void addToken(Socket socket, String token) {
        if (!tokenList.containsKey(token)) {
            tokenList.put(token, socket);
        } else {
            throw new TokenException("存在相同的token: " + token);
        }
    }

    public Socket checkToken(String token, byte[] publicKey) {
        Socket socket = tokenList.get(token);
        if (socket == null) {
            throw new TokenException("token无效");
        } else {
            byte[] pubKey = socket.get(Constant.PUBLIC_KEY);
            Base64.Encoder encoder = Base64.getEncoder();
            if (encoder.encodeToString(pubKey).equalsIgnoreCase(encoder.encodeToString(publicKey))) {
                socket.set(Constant.TOKEN, token);
                tokenList.remove(token);
                return socket;
            } else {
                throw new TokenException("token不匹配: " + token);
            }
        }
    }

    public void status() {
        StringBuffer buffer = new StringBuffer();
        for (String id : linkManager.getLinks().keySet()) {
            String address = null;
            try {
                address = linkManager.getLink(id).getRemoteAddress().toString();
            } catch (IOException e) {
                address = "unknown";
            }
            buffer.append("id: ").append(id).append(" From: ").append(address);
        }
        System.out.println(buffer);
    }

    public void send(String uid, String message) {
        try {
            Socket socket = linkManager.getLink(uid);
            if (socket != null) {
                socket.send(new Message(message));
            } else {
                throw new SocketException("不存在UID: " + uid);
            }
        } catch (SocketException e) {
            log.warn(e.getMessage());
        }
    }

    public abstract void listen();

    public abstract void exit();

}
