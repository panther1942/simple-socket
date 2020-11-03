package cn.erika.test.socket.service;

import cn.erika.socket.core.TcpSocket;
import cn.erika.test.socket.handler.AbstractHandler;
import cn.erika.test.socket.handler.DefineString;
import cn.erika.test.socket.handler.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RequestPublicKey implements SocketService {
    private Logger log = LoggerFactory.getLogger(this.getClass());
    private static final String serviceName = DefineString.REQ_PUBLIC_KEY;

    @Override
    public void service(AbstractHandler handler, TcpSocket socket, Message message) {
        handler.sendMessage(socket, new Message(serviceName, AbstractHandler.getPublicKey()));
//        log.debug("发送客户端公钥: " + Base64.getEncoder().encodeToString(AbstractHandler.getPublicKey()));
    }
}
