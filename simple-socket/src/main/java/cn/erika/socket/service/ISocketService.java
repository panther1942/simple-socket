package cn.erika.socket.service;

import cn.erika.socket.core.TcpSocket;
import cn.erika.socket.handler.AbstractHandler;
import cn.erika.socket.handler.Message;

public interface ISocketService {

    public void client(AbstractHandler handler, TcpSocket socket, Message message);

    public void server(AbstractHandler handler, TcpSocket socket, Message message);
}
