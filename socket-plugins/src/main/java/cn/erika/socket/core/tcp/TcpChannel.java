package cn.erika.socket.core.tcp;

import cn.erika.socket.core.BaseSocket;
import cn.erika.socket.core.component.DataInfo;
import cn.erika.socket.core.component.Message;

import java.io.IOException;
import java.net.SocketAddress;

public class TcpChannel extends BaseSocket {
    @Override
    public void send(DataInfo info) {

    }

    @Override
    public void receive(Message message) {

    }

    @Override
    public SocketAddress getRemoteAddress() {
        return null;
    }

    @Override
    public SocketAddress getLocalAddress() {
        return null;
    }

    @Override
    public boolean isClosed() {
        return false;
    }

    @Override
    public void close() {

    }
}
