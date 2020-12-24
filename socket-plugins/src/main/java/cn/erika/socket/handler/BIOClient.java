package cn.erika.socket.handler;

import cn.erika.config.Constant;
import cn.erika.socket.core.component.Message;
import cn.erika.socket.core.tcp.TcpSocket;
import cn.erika.util.log.Logger;
import cn.erika.util.log.LoggerFactory;

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

    @Override
    public SocketAddress getLocalAddress() {
        return socket.getLocalAddress();
    }

    @Override
    public void close() {
        if (socket != null) {
            socket.send(new Message(Constant.SRV_TEXT, Constant.EXIT));
            socket.close();
        }
    }

    @Override
    public boolean isClosed() {
        return socket != null && socket.isClosed();
    }
}
