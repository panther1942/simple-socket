package cn.erika.test.service;

import cn.erika.socket.core.TcpSocket;
import cn.erika.test.CommonHandler;
import cn.erika.test.DefineString;
import cn.erika.test.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Base64;

public class RequestPublicKey implements SocketService {
    private Logger log = LoggerFactory.getLogger(this.getClass());
    private static final String serviceName = DefineString.REQ_PUBLIC_KEY;

    @Override
    public void service(CommonHandler handler, TcpSocket socket, Message message) {
        handler.sendMessage(socket, new Message(serviceName, CommonHandler.getPublicKey()));
        log.debug("发送客户端公钥: " + Base64.getEncoder().encodeToString(CommonHandler.getPublicKey()));
    }
}
