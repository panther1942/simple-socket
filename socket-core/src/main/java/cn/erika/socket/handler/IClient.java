package cn.erika.socket.handler;

import cn.erika.context.exception.BeanException;
import cn.erika.socket.core.component.Message;

import java.io.IOException;
import java.net.SocketAddress;

public interface IClient {
    public void connect() throws IOException;

    public void send(String message);

    public SocketAddress getLocalAddress();

    public void close();

    public boolean isClosed();

    public void execute(String serviceName, Message message) throws BeanException;
}
