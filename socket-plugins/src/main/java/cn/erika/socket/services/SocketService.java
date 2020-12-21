package cn.erika.socket.services;

import cn.erika.socket.core.Socket;
import cn.erika.socket.core.component.Message;

public interface SocketService {

    public void client(Socket socket, Message message);

    public void server(Socket socket, Message message);
}
