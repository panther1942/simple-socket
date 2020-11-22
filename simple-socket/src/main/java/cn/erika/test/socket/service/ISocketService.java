package cn.erika.test.socket.service;

import cn.erika.socket.core.TcpSocket;
import cn.erika.test.socket.handler.AbstractHandler;
import cn.erika.test.socket.handler.Message;
import cn.erika.test.socket.handler.StringDefine;

public interface ISocketService {

    public void client(AbstractHandler handler, TcpSocket socket, Message message);

    public void server(AbstractHandler handler, TcpSocket socket, Message message);
}
