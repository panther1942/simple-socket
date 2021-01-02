package cn.erika.socket.handler.bio;

import cn.erika.socket.core.tcp.TcpSocket;
import cn.erika.socket.handler.BasicClient;
import cn.erika.socket.handler.IClient;

import java.io.IOException;
import java.net.SocketAddress;

public class BIOClient extends BasicClient implements IClient {
    private SocketAddress address;

    public BIOClient(SocketAddress address) {
        this.address = address;
    }

    @Override
    public void connect() throws IOException {
        this.socket = new TcpSocket(address, this);
    }
}
