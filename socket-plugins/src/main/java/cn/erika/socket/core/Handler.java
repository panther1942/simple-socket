package cn.erika.socket.core;

import cn.erika.socket.core.component.Message;
import cn.erika.socket.exception.ServiceException;

public interface Handler {
    public void init(Socket socket);

    public void onMessage(Socket socket, Message message) throws ServiceException;

    public void onError(Socket socket, Throwable throwable);

}
