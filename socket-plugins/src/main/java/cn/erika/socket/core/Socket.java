package cn.erika.socket.core;

import cn.erika.socket.core.component.DataInfo;
import cn.erika.socket.core.component.Message;

import java.io.IOException;
import java.net.SocketAddress;

public interface Socket {
    public void send(Message message);

    public void receive(DataInfo info);

    public boolean isClosed();

    public void close();

    public SocketAddress getRemoteAddress();

    public SocketAddress getLocalAddress();

    public <T> T set(String k, Object v);

    public <T> T get(String k);

    public <T> T remove(String k);
}
