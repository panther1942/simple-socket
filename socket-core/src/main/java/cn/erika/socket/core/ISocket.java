package cn.erika.socket.core;

import cn.erika.socket.core.component.DataInfo;
import cn.erika.socket.core.component.Message;

import java.net.SocketAddress;

public interface ISocket {
    void send(Message message);

    void receive(DataInfo info);

    boolean isClosed();

    void close();

    SocketAddress getRemoteAddress();

    SocketAddress getLocalAddress();

    <T> T set(String k, Object v);

    <T> T get(String k);

    <T> T remove(String k);

    Handler getHandler();
}
