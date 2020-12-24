package cn.erika.socket.handler;

import cn.erika.socket.core.tcp.TcpSocket;

import java.io.IOException;
import java.net.SocketAddress;

public class BIOClient extends Client {
    private SocketAddress address;

    public BIOClient(SocketAddress address) {
        this.address = address;
    }

    @Override
    public void connect() throws IOException {
        this.socket = new TcpSocket(address, this);
    }
}
