package cn.erika.socket.handler.impl;

import cn.erika.socket.core.TcpSocket;
import cn.erika.socket.handler.AbstractHandler;
import cn.erika.socket.handler.Message;
import cn.erika.socket.service.ISocketService;
import cn.erika.socket.service.NotFoundServiceException;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.SocketException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ServerHandler extends AbstractHandler implements Runnable {
    private ServerSocket server;
    private LinkManager linkManager;

    public ServerHandler(String host, int port) throws IOException {
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
            new Thread(new ServerHandler(address, port)).start();
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

    public void send(String uid, String message) throws SocketException {
        TcpSocket socket = linkManager.getLink(uid);
        if (socket == null) {
            throw new SocketException("连接不存在");
        } else {
            sendMessage(socket, new Message("text", message));
        }
    }

    @Override
    public void onOpen(TcpSocket socket) {
        linkManager.addLink(socket);
        log.info("New client link: " + socket.getSocket().getRemoteSocketAddress());
    }

    @Override
    public void onClose(TcpSocket socket) {
        close(socket);
    }

    @Override
    public void onError(String message, Throwable e) {
        System.err.println(message);
    }

    public void close() {
        try {
            if (!server.isClosed()) {
                server.close();
            }
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
    }

    @Override
    public void deal(TcpSocket socket, Message message) {
        String order = message.getHead(Message.Head.Order);
        ISocketService service = null;
        try {
            service = getService(order);
            service.server(this, socket, message);
        } catch (NotFoundServiceException e) {
            log.error(e.getMessage(), e);
        }
    }

    @Override
    public void close(TcpSocket socket) {
        System.out.println("客户端断开连接");
        if (linkManager.popLink(socket)) {
            try {
                sendMessage(socket, new Message("bye", "exit"));
                socket.close();
            } catch (SocketException e) {
                log.debug("连接中断", e);
            } catch (IOException e) {
                onError(e.getMessage(), e);
            }
        } else {
            System.err.println("未知异常");
        }
    }

    public void displayLink() {
        for (String id : linkManager.linkList.keySet()) {
            System.out.println("id: " + id + " From: " + linkManager.getLink(id).getSocket().getRemoteSocketAddress());
        }
    }

    public TcpSocket getSocket(String uid) {
        return linkManager.getLink(uid);
    }

    private class LinkManager {
        private Map<String, TcpSocket> linkList = new HashMap<>();

        TcpSocket addLink(TcpSocket socket) {
            String uuid = UUID.randomUUID().toString();
            socket.set("id", uuid);
            linkList.put(uuid, socket);
            return socket;
        }

        TcpSocket getLink(String uid) {
            return linkList.get(uid);
        }

        TcpSocket delLink(String uid) {
            return linkList.remove(uid);
        }

        String isExistLink(TcpSocket socket) {
            for (String uid : linkList.keySet()) {
                if (socket.equals(linkList.get(uid))) {
                    return uid;
                }
            }
            return null;
        }

        boolean popLink(TcpSocket socket) {
            String uid = isExistLink(socket);
            if (uid != null) {
                delLink(uid);
                return true;
            }
            return false;
        }
    }
}
