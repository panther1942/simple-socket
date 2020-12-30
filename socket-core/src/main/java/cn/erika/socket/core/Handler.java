package cn.erika.socket.core;

import cn.erika.context.exception.BeanException;
import cn.erika.socket.core.component.Message;

public interface Handler {
    void init(ISocket socket);

    void onMessage(ISocket socket, Message message) throws BeanException;

    void onError(ISocket socket, Throwable throwable);

    void onClose(ISocket socket);

    void close();

    boolean isClosed();

}
