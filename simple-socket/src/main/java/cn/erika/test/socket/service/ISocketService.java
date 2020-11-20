package cn.erika.test.socket.service;

import cn.erika.socket.core.TcpSocket;
import cn.erika.test.socket.handler.AbstractHandler;
import cn.erika.test.socket.handler.Message;
import cn.erika.test.socket.handler.StringDefine;

public interface ISocketService {

    public void request(AbstractHandler handler, TcpSocket socket, Message message);

    public void response(AbstractHandler handler, TcpSocket socket, Message message);
}
