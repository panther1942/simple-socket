package cn.erika.socket.handler;

import cn.erika.socket.core.ISocket;

import java.net.SocketAddress;

public interface IServer {

    public void addToken(ISocket socket, String token);

    public ISocket checkToken(String token, byte[] publicKey);

    public void status();

    public void send(String uid, String message);

    public void listen();

    public void close();

    public boolean isClosed();

    public SocketAddress getLocalAddress();
}
