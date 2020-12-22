package cn.erika.socket.handler;

import cn.erika.socket.core.tcp.TcpSocket;
import cn.erika.util.log.Logger;
import cn.erika.util.log.LoggerFactory;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.SocketAddress;

public class BIOServer extends Server implements Runnable {
    private Logger log = LoggerFactory.getLogger(this.getClass());
    private ServerSocket server;

    public BIOServer(SocketAddress address) {
        try {
            this.server = new ServerSocket();
            this.server.bind(address);
            log.info("服务器监听: " + address.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        while (!server.isClosed()) {
            try {
                new TcpSocket(server.accept(), this);
            } catch (IOException e) {
                log.error(e.getMessage());
            }
        }
        log.warn("运行中断");
    }

    @Override
    public void listen() {
        Thread t = new Thread(this);
        t.setDaemon(true);
        t.start();
    }

    @Override
    public void exit() {
        try {
            if (!server.isClosed()) {
                server.close();
                log.info("关闭服务器");
            }
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    @Override
    public void close() {
        try {
            this.server.close();
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    public boolean isClosed() {
        return this.server.isClosed();
    }

    @Override
    public SocketAddress getLocalAddress() {
        return this.server.getLocalSocketAddress();
    }
}
