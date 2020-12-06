package cn.erika.socket.common.component;

import java.net.Socket;

public interface BaseSocket {
    public void send(Message message);

    public void receive(DataInfo info, byte[] data);

    public void ready();

    public Socket getSocket();

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
