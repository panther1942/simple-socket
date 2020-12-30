package cn.erika.socket.handler;

import cn.erika.socket.core.ISocket;
import cn.erika.socket.exception.AuthenticateException;

import java.net.SocketAddress;

public interface IServer {

    void addToken(ISocket socket, String token) throws AuthenticateException;

    ISocket checkToken(String token, byte[] publicKey) throws AuthenticateException;

    void status();

    void send(String uid, String message);

    void listen();

    void close();

    boolean isClosed();

    SocketAddress getLocalAddress();
}
