package cn.erika.socket.handler.bio;

import cn.erika.socket.core.tcp.TcpSocket;
import cn.erika.socket.handler.BaseServer;
import cn.erika.socket.handler.IServer;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.SocketAddress;
import java.net.SocketException;

public class BIOServer extends BaseServer implements IServer {
    private ServerSocket server;

    public BIOServer(SocketAddress address) throws IOException {
        this.server = new ServerSocket();
        this.server.bind(address);
        log.info("服务器监听端口: " + getLocalAddress());
    }

    @Override
    public void run() {
        while (!server.isClosed()) {
            try {
                new TcpSocket(server.accept(), this);
            } catch (SocketException e) {
                log.info("服务器停止运行");
            } catch (IOException e) {
                e.printStackTrace();
                log.error(e.getMessage());
            }
        }
    }

    @Override
    public void close() {
        super.close();
        try {
            if (server != null && !server.isClosed()) {
                server.close();
                log.info("关闭服务器");
            }
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    @Override
    public boolean isClosed() {
        return server != null && server.isClosed();
    }

    @Override
    public SocketAddress getLocalAddress() {
        return server.getLocalSocketAddress();
    }
}
