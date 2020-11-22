package cn.erika.socket.handler.impl;

import cn.erika.socket.core.TcpSocket;
import cn.erika.socket.handler.AbstractHandler;
import cn.erika.socket.handler.Message;
import cn.erika.socket.handler.StringDefine;
import cn.erika.socket.service.ISocketService;
import cn.erika.socket.service.NotFoundServiceException;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Scanner;

public class ClientHandler extends AbstractHandler {
    private InetSocketAddress address;
    private TcpSocket socket;

    public ClientHandler(String address, int port) {
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
        ClientHandler clientHandler = new ClientHandler(address, port);
        clientHandler.connect();

        Scanner scanner = new Scanner(System.in);
        String line = null;
        while ((line = scanner.nextLine()) != null) {
            if (!"exit".equals(line)) {
                clientHandler.send(line);
            } else {
                clientHandler.close();
            }
        }
    }

    public void send(String message) {
        sendMessage(socket, new Message("text", message));
    }

    @Override
    public void onOpen(TcpSocket socket) {
        System.out.println("成功连接到服务器");
        try {
            ISocketService service = getService(StringDefine.SEVR_PUBLIC_KEY);
            service.client(this, socket, null);
        } catch (NotFoundServiceException e) {
            log.error(e.getMessage(), e);
        }
    }

    @Override
    public void onClose(TcpSocket socket) {
        if (socket == null) {
            return;
        }
        String host = socket.getSocket().getInetAddress().getHostAddress();
        System.out.println("正在关闭连接 From: " + host);
        close(socket);
    }

    @Override
    public void onError(String message, Throwable error) {
        System.err.println(message);
    }

    public void close() {
        close(this.socket);
    }

    @Override
    public void deal(TcpSocket socket, Message message) {
        String order = message.getHead(Message.Head.Order);
        ISocketService service = null;
        try {
            service = getService(order);
            service.client(this, socket, message);
        } catch (NotFoundServiceException e) {
            log.error(e.getMessage(), e);
        }
    }

    @Override
    public void close(TcpSocket socket) {
        try {
            if (!socket.getSocket().isClosed()) {
                sendMessage(socket, new Message("exit", "exit"));
                socket.close();
            }
        } catch (IOException e) {
            onError("连接中断", e);
        }
    }
}
