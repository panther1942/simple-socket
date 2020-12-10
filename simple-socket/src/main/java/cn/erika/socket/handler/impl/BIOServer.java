package cn.erika.socket.handler.impl;

import cn.erika.config.Constant;
import cn.erika.socket.common.component.BaseSocket;
import cn.erika.socket.common.component.Message;
import cn.erika.socket.core.TcpSocket;
import cn.erika.socket.handler.IServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.SocketException;

public class BIOServer extends AbstractServerHandler implements IServer, Runnable {
    private ServerSocket server;

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
    public void onOpen(BaseSocket socket) throws IOException {
        linkManager.addLink(socket);
        log.info("New client link: " + socket.getRemoteAddress());
        socket.set(Constant.TYPE, Constant.SERVER);
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
}
