package cn.erika.socket.handler;

import cn.erika.config.Constant;
import cn.erika.socket.core.component.Message;
import cn.erika.socket.core.tcp.TcpSocket;
import cn.erika.util.log.Logger;
import cn.erika.util.log.LoggerFactory;

import java.io.IOException;
import java.net.SocketAddress;

public class BIOClient extends Client {
    private Logger log = LoggerFactory.getLogger(this.getClass());
    private SocketAddress address;

    public BIOClient(SocketAddress address) {
        this.address = address;
    }

    @Override
    public void connect() {
        try {
            this.socket = new TcpSocket(address, this);
        } catch (IOException e) {
            onError(socket, e);
        }
    }

    @Override
    public void disconnect() {
        socket.send(new Message(Constant.SRV_TEXT, Constant.EXIT));
        socket.close();
    }

    @Override
    public SocketAddress getLocalAddress() {
        try {
            return socket.getLocalAddress();
        } catch (IOException e) {
            log.error("无法获取本地地址");
        }
        return null;
    }

    @Override
    public void close() {
        if (socket != null) {
            socket.close();
        }
    }

    @Override
    public boolean isClosed() {
        return socket.isClosed();
    }
}
