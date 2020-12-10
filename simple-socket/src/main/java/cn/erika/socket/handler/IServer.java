package cn.erika.socket.handler;

import cn.erika.socket.common.component.BaseSocket;
import cn.erika.socket.common.exception.TokenException;

public interface IServer extends Runnable {

    public void close();

    public void displayLink();

    public void send(String uid, String message);

    public void addToken(BaseSocket socket, String token) throws TokenException;

    public BaseSocket checkToken(String token, byte[] publicKey) throws TokenException;
}
