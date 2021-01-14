package cn.erika.socket.core;

import cn.erika.socket.model.pto.DataInfo;
import cn.erika.socket.model.pto.Message;

import java.net.SocketAddress;

public interface ISocket {
    boolean send(Message message);

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
