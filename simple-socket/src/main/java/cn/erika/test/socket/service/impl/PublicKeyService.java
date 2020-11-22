package cn.erika.test.socket.service.impl;

import cn.erika.socket.core.TcpSocket;
import cn.erika.test.socket.handler.AbstractHandler;
import cn.erika.test.socket.handler.Message;
import cn.erika.test.socket.handler.StringDefine;
import cn.erika.test.socket.service.ISocketService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PublicKeyService implements ISocketService {
    private static final String serviceName = StringDefine.SEVR_PUBLIC_KEY;
    private Logger log = LoggerFactory.getLogger(this.getClass());

    @Override
    public void client(AbstractHandler handler, TcpSocket socket, Message message) {
        Message request = new Message(serviceName, AbstractHandler.getPublicKey());
        handler.sendMessage(socket, request);
//        log.debug("发送公钥: " + Base64.getEncoder().encodeToString(AbstractHandler.getPublicKey()));
    }

    @Override
    public void server(AbstractHandler handler, TcpSocket socket, Message message) {
        socket.set(StringDefine.PUBLIC_KEY, message.getPayload());
        Message response = new Message(StringDefine.SEVR_EXCHANGE_KEY, AbstractHandler.getPublicKey());
        handler.sendMessage(socket, response);
        log.debug("对端请求加密通信");
//        log.debug("获得公钥: " + Base64.getEncoder().encodeToString(message.getPayload()));
//        log.debug("发送公钥: " + Base64.getEncoder().encodeToString(AbstractHandler.getPublicKey()));
    }
}
