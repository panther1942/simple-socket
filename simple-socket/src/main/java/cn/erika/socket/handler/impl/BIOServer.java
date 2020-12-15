package cn.erika.socket.handler.impl;

import cn.erika.socket.core.TcpSocket;
import cn.erika.socket.handler.IServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;

public class BIOServer extends AbstractServerHandler implements IServer, Runnable {
    private ServerSocket server;

    public BIOServer(InetSocketAddress address) throws IOException {
        this.linkManager = new LinkManager();
        this.server = new ServerSocket();
        try {
            server.bind(address);
            log.info("Listen: " + address.getAddress());
        } catch (IOException e) {
            onError(e.getMessage(), e);
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
        log.warn("运行中断");
    }

    @Override
    public boolean isClosed() {
        return server.isClosed();
    }

    @Override
    public void close() {
        try {
            if (!server.isClosed()) {
                server.close();
                log.info("关闭服务器");
            }
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }
}
