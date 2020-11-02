package cn.erika.test.service;

import cn.erika.socket.core.TcpSocket;
import cn.erika.test.CommonHandler;
import cn.erika.test.DefineString;
import cn.erika.test.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Base64;

public class ResponsePublicKey implements SocketService {
    private Logger log = LoggerFactory.getLogger(this.getClass());
    private static final String serviceName = DefineString.RESP_PUBLIC_KEY;

    @Override
    public void service(CommonHandler handler, TcpSocket socket, Message message) {
        socket.set(DefineString.PUBLIC_KEY, message.getPayload());
        handler.sendMessage(socket, new Message(serviceName, CommonHandler.getPublicKey()));
        log.debug("客户端请求加密通信");
        log.debug("获得客户端公钥: " + Base64.getEncoder().encodeToString(message.getPayload()));
        log.debug("发送服务器公钥: " + Base64.getEncoder().encodeToString(CommonHandler.getPublicKey()));
    }
}
