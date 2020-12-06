package cn.erika.socket.handler.impl;

import cn.erika.aop.exception.BeanException;
import cn.erika.cli.App;
import cn.erika.socket.core.TcpSocket;
import cn.erika.socket.common.component.BaseSocket;
import cn.erika.socket.common.component.Message;
import cn.erika.config.Constant;
import cn.erika.socket.handler.IClient;

import java.io.IOException;
import java.net.InetSocketAddress;

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

    @Override
    public void send(String message) {
        Message msg = new Message(Constant.TEXT, message);
        socket.send(msg);
    }

    @Override
    public void onOpen(BaseSocket socket) throws BeanException {
        System.out.println("成功连接到服务器");
        socket.set(Constant.TYPE, Constant.CLIENT);
        App.execute(socket, Constant.SRV_EXCHANGE_KEY, socket, null);
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

    public void close(BaseSocket socket) {
        if (!socket.getSocket().isClosed()) {
            Message msg = new Message(Constant.EXIT);
            socket.send(msg);
            socket.close();
        }
    }
}
