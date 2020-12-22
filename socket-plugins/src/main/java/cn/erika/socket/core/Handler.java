package cn.erika.socket.core;

import cn.erika.context.exception.BeanException;
import cn.erika.socket.core.component.Message;

public interface Handler {
    public void init(Socket socket);

    public void onMessage(Socket socket, Message message) throws BeanException;

    public void onError(Socket socket, Throwable throwable);

    public void onClose(Socket socket);

    public void close();

    public boolean isClosed();

}
