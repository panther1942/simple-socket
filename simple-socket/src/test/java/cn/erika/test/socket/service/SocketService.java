package cn.erika.test.socket.service;

import cn.erika.socket.core.TcpSocket;
import cn.erika.test.socket.handler.AbstractHandler;
import cn.erika.test.socket.handler.Message;

public interface SocketService {
    void service(AbstractHandler handler, TcpSocket socket, Message message);
}
