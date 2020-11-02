package cn.erika.test;

import cn.erika.socket.core.TcpSocket;
import cn.erika.test.service.*;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.SocketException;
import java.util.HashMap;
import java.util.Map;

public class Server extends CommonHandler implements Runnable {
    private static Map<String, SocketService> serviceList = new HashMap<>();
    private Map<String, TcpSocket> linkList = new HashMap<>();
    private ServerSocket server;
    private int count = 0;

    static {
        register(DefineString.REQ_PUBLIC_KEY, new ResponsePublicKey());
        register(DefineString.REQ_ENCRYPT, new ResponseEncrypt());
    }

    public static void register(String serviceName, SocketService service) {
        serviceList.put(serviceName, service);
    }

    public Server(String host, int port) throws IOException {
        InetSocketAddress address = new InetSocketAddress(host, port);
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
        System.out.println("New client link: " + socket.getSocket().getRemoteSocketAddress());
//        sendMessage(socket, new Message("hello world", "text"));
    }

    @Override
    public void onClose(TcpSocket socket) {
        System.out.println("客户端断开连接");
        if (popLink(socket)) {
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

    @Override
    public void onError(String message, Throwable e) {
        System.err.println(message);
    }

    @Override
    public void deal(TcpSocket socket, Message message) {
        String order = message.getHead(Message.Head.REQUEST);
        SocketService service = serviceList.get(order);
        if (service != null) {
            service.service(this, socket, message);
        } else {
            System.out.println(new String(message.getPayload(), CHARSET));
        }
    }
}
