package cn.erika.test;

import cn.erika.socket.core.TcpSocket;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.util.HashMap;
import java.util.Map;

public class Server extends HandlerImpl implements Runnable {
    private Map<String, TcpSocket> linkList = new HashMap<>();
    private ServerSocket server;
    private int count = 0;

    public Server(String address, int port) throws IOException {
        InetSocketAddress address1 = new InetSocketAddress(address, port);
        this.server = new ServerSocket();
        try {
            server.bind(address1);
            System.out.println("Listen: " + address1.getAddress());
        } catch (IOException e) {
            onError(e.getMessage(), e);
        }
    }

    public static void main(String[] args) {
        String address = "localhost";
        int port = 12345;
        try {
            new Thread(new Server(address, port)).start();
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

    public TcpSocket addLink(TcpSocket socket) {
        linkList.put(randomUid(), socket);
        return socket;
    }

    public TcpSocket getLink(String uid) {
        return linkList.get(uid);
    }

    public TcpSocket delLink(String uid) {
        return linkList.remove(uid);
    }

    public String isExistLink(TcpSocket socket) {
        for (String uid : linkList.keySet()) {
            if (socket.equals(linkList.get(uid))) {
                return uid;
            }
        }
        return null;
    }

    public boolean popLink(TcpSocket socket) {
        String uid = isExistLink(socket);
        if (uid != null) {
            delLink(uid);
            return true;
        }
        return false;
    }

    private String randomUid() {
        return String.valueOf(count++);
    }

    @Override
    public void onOpen(TcpSocket socket) {
        addLink(socket);
        sendMessage(socket, "hello world");
    }

    @Override
    public void onClose(TcpSocket socket) {
        if (popLink(socket)) {
            try {
                sendMessage(socket, "bye");
                socket.close();
            } catch (IOException e) {
                onError(e.getMessage(), e);
            }
        } else {
            System.err.println("未知异常");
        }
    }

    @Override
    public void onError(String message, Throwable error) {
        System.err.println(message);
    }
}
