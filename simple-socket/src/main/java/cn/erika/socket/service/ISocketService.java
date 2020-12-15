package cn.erika.socket.service;

import cn.erika.socket.core.BaseSocket;
import cn.erika.socket.component.Message;

public interface ISocketService {

    public void client(BaseSocket socket, Message message);

    public void server(BaseSocket socket, Message message);
}
