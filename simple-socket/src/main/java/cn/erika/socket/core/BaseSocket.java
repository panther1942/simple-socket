package cn.erika.socket.core;

import cn.erika.socket.component.Message;

import java.io.IOException;
import java.net.SocketAddress;

public interface BaseSocket {
    public void send(Message message);

    public void send(byte[] data) throws IOException;

    public void receive(DataInfo info, byte[] data);

    public void ready();

    public SocketAddress getRemoteAddress() throws IOException;

    public boolean isClosed();

    public void close();

    // 设置连接额外属性
    @SuppressWarnings("unchecked")
    public <T> T set(String k, Object v);
    // 获取连接额外属性

    @SuppressWarnings("unchecked")
    public <T> T get(String k);
    // 移除连接额外属性

    @SuppressWarnings("unchecked")
    public <T> T remove(String k);
}
