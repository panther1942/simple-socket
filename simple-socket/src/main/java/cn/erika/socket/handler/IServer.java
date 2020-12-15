package cn.erika.socket.handler;

import cn.erika.socket.core.BaseSocket;
import cn.erika.socket.exception.TokenException;

public interface IServer extends Runnable {

    public void listen();

    public boolean isClosed();

    public void close();

    public void displayLink();

    public void send(String uid, String message);

    public void addToken(BaseSocket socket, String token) throws TokenException;

    public BaseSocket checkToken(String token, byte[] publicKey) throws TokenException;
}
