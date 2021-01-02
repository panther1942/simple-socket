package cn.erika.socket.handler.bio;

import cn.erika.context.exception.BeanException;
import cn.erika.socket.core.ISocket;
import cn.erika.socket.core.tcp.TcpSocket;
import cn.erika.socket.handler.BaseClient;
import cn.erika.socket.handler.IClient;

import java.io.IOException;
import java.net.SocketAddress;

public class BIOClient extends BaseClient implements IClient {
    private SocketAddress address;

    public BIOClient(SocketAddress address) {
        this.address = address;
    }

    @Override
    public void connect() throws IOException {
        this.socket = new TcpSocket(address, this);
    }

    @Override
    public void init(ISocket socket) {
        super.init(socket);
        try {
            onConnected(socket);
        } catch (BeanException e) {
            log.error("无法初始化连接: " + e.getMessage(), e);
        }
    }
}
