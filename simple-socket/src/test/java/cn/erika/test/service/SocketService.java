package cn.erika.test.service;

import cn.erika.socket.core.TcpSocket;
import cn.erika.test.CommonHandler;
import cn.erika.test.Message;

public interface SocketService {
    void service(CommonHandler handler, TcpSocket socket, Message message);
}
