package cn.erika.socket.handler;

import cn.erika.context.exception.BeanException;
import cn.erika.socket.core.component.Message;

import java.io.IOException;
import java.net.SocketAddress;

public interface IClient {
    void connect() throws IOException;

    void send(String message);

    SocketAddress getLocalAddress();

    void close();

    boolean isClosed();

    void execute(String serviceName, Message message) throws BeanException;
}
