package cn.erika.socket.handler;

import cn.erika.config.Constant;
import cn.erika.socket.core.BaseHandler;
import cn.erika.socket.core.Socket;
import cn.erika.socket.core.component.LinkManager;
import cn.erika.socket.core.component.Message;
import cn.erika.socket.exception.TokenException;
import cn.erika.util.security.MessageDigest;

import java.net.SocketException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class Server extends BaseHandler implements Runnable {
    private LinkManager linkManager = new LinkManager();
    private Map<String, Socket> tokenList = new ConcurrentHashMap<>();

    @Override
    public void init(Socket socket) {
        super.init(socket);
        log.info("新连接接入: " + socket.getRemoteAddress());
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
            long srcToken = MessageDigest.crc32Sum(pubKey);
            long targetToken = MessageDigest.crc32Sum(publicKey);
            if (srcToken == targetToken) {
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
            address = linkManager.getLink(id).getRemoteAddress().toString();
            buffer.append("id: ").append(id).append(" From: ").append(address);
        }
        log.info(buffer.toString());
    }

    public void send(String uid, String message) {
        try {
            Socket socket = linkManager.getLink(uid);
            if (socket != null) {
                socket.send(new Message(Constant.SRV_TEXT, message));
            } else {
                throw new SocketException("不存在UID: " + uid);
            }
        } catch (SocketException e) {
            log.warn(e.getMessage());
        }
    }

    public void listen() {
        Thread t = new Thread(this, this.getClass().getSimpleName());
        t.setName(this.getClass().getSimpleName());
        t.setDaemon(true);
        t.start();
    }

    public abstract void close();

}
