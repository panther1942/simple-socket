package cn.erika.test;

import cn.erika.socket.core.TcpSocket;

import java.io.IOException;
import java.net.InetSocketAddress;

public class Client extends HandlerImpl {
    private InetSocketAddress address;
    private TcpSocket socket;

    public Client(String address, int port) {
        this.address = new InetSocketAddress(address, port);
    }

    public void connect() {
        try {
            this.socket = new TcpSocket(address, this, CHARSET);
        } catch (IOException e) {
            onError(e.getMessage(), e);
        }
    }

    public static void main(String[] args) {
        String address = "localhost";
        int port = 12345;
        Client client = new Client(address, port);
        client.connect();
        client.send("你好 再见");
        client.send("exit");
    }

    public void send(String message) {
        sendMessage(socket, message);
    }

    @Override
    public void onOpen(TcpSocket socket) {
        System.out.println("成功连接到服务器");
    }

    @Override
    public void onClose(TcpSocket socket) {
        if (socket == null) {
            return;
        }
        String host = socket.getSocket().getInetAddress().getHostAddress();
        System.out.println("正在关闭连接 From: " + host);
        try {
            if (!socket.getSocket().isClosed()) {
                sendMessage(socket, "exit");
                socket.close();
            }
        } catch (IOException e) {
            onError("连接中断", e);
        }
    }

    @Override
    public void onError(String message, Throwable error) {
        System.err.println(message);
    }
}
