package cn.erika.socket.handler.impl;

import cn.erika.config.Constant;
import cn.erika.socket.core.BaseSocket;
import cn.erika.socket.component.Message;
import cn.erika.socket.exception.TokenException;
import cn.erika.socket.handler.IServer;

import java.io.IOException;
import java.net.SocketException;
import java.util.Base64;
import java.util.concurrent.ConcurrentHashMap;

public abstract class AbstractServerHandler extends AbstractHandler implements IServer {
    protected LinkManager linkManager;
    private ConcurrentHashMap<String, BaseSocket> tokenList = new ConcurrentHashMap<>();

    @Override
    public void displayLink() {
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

    @Override
    public void send(String uid, String message) {
        BaseSocket socket = linkManager.getLink(uid);
        try {
            if (socket == null) {
                throw new SocketException("连接不存在");
            } else {
                socket.send(new Message(Constant.SRV_TEXT, message));
            }
        } catch (SocketException e) {
            log.warn("UID: " + uid + " 不存在");
        }
    }

    @Override
    public void onOpen(BaseSocket socket) throws IOException {
        linkManager.addLink(socket);
        log.info("New client link: " + socket.getRemoteAddress());
    }

    @Override
    public void onClose(BaseSocket socket) {
        close(socket);
    }

    @Override
    public void listen() {
        Thread thread = new Thread(this);
        thread.setDaemon(true);
        thread.start();
    }

    public void close(BaseSocket socket) {
        if (linkManager.popLink(socket)) {
            socket.send(new Message(Constant.EXIT, Constant.EXIT));
            socket.close();
        }
    }

    @Override
    public void addToken(BaseSocket socket, String token) throws TokenException {
        if (!tokenList.containsKey(token)) {
            tokenList.put(token, socket);
        } else {
            throw new TokenException("存在相同的token: " + token);
        }
    }

    @Override
    public BaseSocket checkToken(String token, byte[] publicKey) throws TokenException {
        BaseSocket socket = tokenList.get(token);
        if (socket == null) {
            throw new TokenException("token无效");
        } else {
            byte[] pubKey = socket.get(Constant.PUBLIC_KEY);
            Base64.Encoder encoder = Base64.getEncoder();
            if (encoder.encodeToString(pubKey).equalsIgnoreCase(encoder.encodeToString(publicKey))) {
                socket.set(Constant.SESSION_TOKEN, token);
                tokenList.remove(token);
                return socket;
            } else {
                throw new TokenException("token不匹配: " + token);
            }
        }
    }
}
