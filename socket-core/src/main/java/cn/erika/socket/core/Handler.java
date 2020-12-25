package cn.erika.socket.core;

import cn.erika.context.exception.BeanException;
import cn.erika.socket.core.component.Message;

public interface Handler {
    public void init(ISocket socket);

    public void onMessage(ISocket socket, Message message) throws BeanException;

    public void onError(ISocket socket, Throwable throwable);

    public void onClose(ISocket socket);

    public void close();

    public boolean isClosed();

}
