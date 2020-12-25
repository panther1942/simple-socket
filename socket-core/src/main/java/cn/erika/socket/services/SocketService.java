package cn.erika.socket.services;

import cn.erika.socket.core.ISocket;
import cn.erika.socket.core.component.Message;

public interface SocketService {

    public void client(ISocket socket, Message message);

    public void server(ISocket socket, Message message);
}
