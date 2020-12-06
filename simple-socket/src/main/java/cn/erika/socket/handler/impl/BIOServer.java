package cn.erika.socket.handler.impl;

import cn.erika.socket.common.exception.TokenException;
import cn.erika.socket.core.TcpSocket;
import cn.erika.socket.common.component.BaseSocket;
import cn.erika.socket.common.component.Message;
import cn.erika.config.Constant;
import cn.erika.socket.handler.IServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.SocketException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

public class BIOServer extends AbstractHandler implements IServer, Runnable {
    private ServerSocket server;
    private LinkManager linkManager;
    private Map<String, BaseSocket> tokenList = new LinkedHashMap<>();

    public BIOServer(String host, int port) throws IOException {
        InetSocketAddress address = new InetSocketAddress(host, port);
        this.linkManager = new LinkManager();
        this.server = new ServerSocket();
        try {
            server.bind(address);
            System.out.println("Listen: " + address.getAddress());
        } catch (IOException e) {
            onError(e.getMessage(), e);
        }
    }

    public static void main(String[] args) {
        String address = "localhost";
        int port = 12345;
        try {
            new Thread(new BIOServer(address, port)).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        while (!server.isClosed()) {
            try {
                new TcpSocket(server.accept(), this, CHARSET);
            } catch (IOException e) {
                onError(e.getMessage(), e);
            }
        }
        System.out.println("运行中断");
    }

    @Override
    public void send(String uid, String message) {
        BaseSocket socket = linkManager.getLink(uid);
        try {
            if (socket == null) {
                throw new SocketException("连接不存在");
            } else {
                socket.send(new Message(Constant.TEXT, message));
            }
        } catch (SocketException e) {
            log.warn("UID: " + uid + " 不存在");
        }
    }

    @Override
    public void onOpen(BaseSocket socket) {
        linkManager.addLink(socket);
        log.info("New client link: " + socket.getSocket().getRemoteSocketAddress());
        socket.set(Constant.TYPE, Constant.SERVER);
    }

    @Override
    public void onClose(BaseSocket socket) {
        close(socket);
    }

    @Override
    public void onError(String message, Throwable e) {
        System.err.println(message);
    }

    @Override
    public void close() {
        try {
            if (!server.isClosed()) {
                server.close();
            }
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
    }

    public void close(BaseSocket socket) {
        if (linkManager.popLink(socket)) {
            socket.send(new Message(Constant.EXIT, "exit"));
            socket.close();
        } else {
            System.err.println("未知异常");
        }
    }

    @Override
    public void displayLink() {
        for (String id : linkManager.linkList.keySet()) {
            System.out.println("id: " + id + " From: " + linkManager.getLink(id).getSocket().getRemoteSocketAddress());
        }
    }

    @Override
    public void addToken(BaseSocket socket, String token) throws TokenException {
        if (!token.contains(token)) {
            tokenList.put(token, socket);
        } else {
            throw new TokenException("存在相同的token");
        }
    }

    @Override
    public void checkToken(String token, String publicKey) throws TokenException {
        BaseSocket socket = tokenList.get(token);
        if (socket == null) {
            throw new TokenException("token无效");
        } else {
            String originKey = socket.get(Constant.PUBLIC_KEY);
            if (originKey.equalsIgnoreCase(publicKey)) {
                tokenList.remove(token);
            } else {
                throw new TokenException("token不匹配");
            }
        }
    }

    private class LinkManager {
        private Map<String, BaseSocket> linkList = new HashMap<>();

        BaseSocket addLink(BaseSocket socket) {
            String uuid = UUID.randomUUID().toString();
            socket.set("id", uuid);
            linkList.put(uuid, socket);
            return socket;
        }

        BaseSocket getLink(String uid) {
            return linkList.get(uid);
        }

        BaseSocket delLink(String uid) {
            return linkList.remove(uid);
        }

        String isExistLink(BaseSocket socket) {
            for (String uid : linkList.keySet()) {
                if (socket.equals(linkList.get(uid))) {
                    return uid;
                }
            }
            return null;
        }

        boolean popLink(BaseSocket socket) {
            String uid = isExistLink(socket);
            if (uid != null) {
                delLink(uid);
                return true;
            }
            return false;
        }
    }
}
