package cn.erika.socket.handler.impl;

import cn.erika.aop.exception.BeanException;
import cn.erika.cli.App;
import cn.erika.socket.bio.core.AbstractHandler;
import cn.erika.socket.bio.core.TcpSocket;
import cn.erika.socket.common.component.BaseSocket;
import cn.erika.socket.common.component.Message;
import cn.erika.config.Constant;
import cn.erika.socket.handler.IClient;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Scanner;

public class BIOClient extends AbstractHandler implements IClient {
    private InetSocketAddress address;
    private TcpSocket socket;

    public BIOClient(String address, int port) {
        this.address = new InetSocketAddress(address, port);
    }

    @Override
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
        BIOClient client = new BIOClient(address, port);
        client.connect();

        Scanner scanner = new Scanner(System.in);
        String line = null;
        while ((line = scanner.nextLine()) != null) {
            if (!"exit".equals(line)) {
                client.send(line);
            } else {
                client.close();
            }
        }
    }

    @Override
    public void send(String message) {
        Message msg = new Message(Constant.TEXT, message);
        socket.send(msg);
    }

    @Override
    public void onOpen(BaseSocket socket) {
        System.out.println("成功连接到服务器");
        socket.set(Constant.TYPE, Constant.CLIENT);

        try {
            App.execute(socket, Constant.SRV_EXCHANGE_KEY, socket, null);
        } catch (BeanException e) {
            log.warn(e.getMessage());
        }
    }

    @Override
    public void onClose(BaseSocket socket) {
        if (socket == null || socket.isClosed()) {
            return;
        }
        String host = socket.getSocket().getInetAddress().getHostAddress();
        System.out.println("正在关闭连接 From: " + host);
        close(socket);
    }

    @Override
    public void onError(String message, Throwable error) {
        log.error(message, error);
    }

    @Override
    public void close() {
        close(this.socket);
    }

    @Override
    public void close(BaseSocket socket) {
        if (!socket.getSocket().isClosed()) {
            Message msg = new Message(Constant.EXIT);
            socket.send(msg);
            socket.close();
        }
    }
}
