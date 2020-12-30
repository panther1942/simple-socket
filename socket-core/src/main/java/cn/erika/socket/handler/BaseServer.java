package cn.erika.socket.handler;

import cn.erika.config.Constant;
import cn.erika.socket.core.BaseHandler;
import cn.erika.socket.core.ISocket;
import cn.erika.socket.core.component.LinkManager;
import cn.erika.socket.core.component.Message;
import cn.erika.socket.exception.AuthenticateException;
import cn.erika.socket.exception.TokenException;
import cn.erika.util.security.MessageDigestUtils;

import java.net.SocketAddress;
import java.net.SocketException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class BaseServer extends BaseHandler implements Runnable, IServer {
    private LinkManager linkManager = new LinkManager();
    private Map<String, ISocket> tokenList = new ConcurrentHashMap<>();

    @Override
    public void init(ISocket socket) {
        super.init(socket);
        log.info("新连接接入: " + socket.getRemoteAddress());
        linkManager.addLink(socket);
    }

    @Override
    public void onClose(ISocket socket) {
        log.info("客户端离线: " + socket.get(Constant.UID));
        linkManager.popLink(socket);
    }

    @Override
    public void addToken(ISocket socket, String token) throws AuthenticateException {
        if (!tokenList.containsKey(token)) {
            tokenList.put(token, socket);
        } else {
            throw new TokenException("存在相同的token: " + token);
        }
    }

    @Override
    public ISocket checkToken(String token, byte[] publicKey) throws AuthenticateException {
        ISocket socket = tokenList.get(token);
        if (socket == null) {
            throw new TokenException("token无效");
        } else {
            byte[] pubKey = socket.get(Constant.PUBLIC_KEY);
            long srcToken = MessageDigestUtils.crc32Sum(pubKey);
            long targetToken = MessageDigestUtils.crc32Sum(publicKey);
            if (srcToken == targetToken) {
                tokenList.remove(token);
                return socket;
            } else {
                throw new TokenException("token不匹配: " + token);
            }
        }
    }

    @Override
    public void status() {
        StringBuffer buffer = new StringBuffer();
        for (String id : linkManager.getLinks().keySet()) {
            SocketAddress address = linkManager.getLink(id).getRemoteAddress();
            buffer.append("id: ").append(id).append(" From: ").append(address).append(System.lineSeparator());
        }
        log.info(buffer.toString());
    }

    @Override
    public void send(String uid, String message) {
        try {
            ISocket socket = linkManager.getLink(uid);
            if (socket != null) {
                socket.send(new Message(Constant.SRV_TEXT, message));
            } else {
                throw new SocketException("不存在UID: " + uid);
            }
        } catch (SocketException e) {
            log.warn(e.getMessage());
        }
    }

    @Override
    public void listen() {
        Thread t = new Thread(this, this.getClass().getSimpleName());
        t.setName(this.getClass().getSimpleName());
        t.setDaemon(true);
        t.start();
    }

}
