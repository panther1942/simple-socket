package cn.erika.socket.bio.service;

import cn.erika.socket.bio.core.TcpSocket;
import cn.erika.socket.bio.handler.AbstractHandler;
import cn.erika.socket.bio.handler.Message;

public interface ISocketService {

    public void client(AbstractHandler handler, TcpSocket socket, Message message);

    public void server(AbstractHandler handler, TcpSocket socket, Message message);
}
