package cn.erika.test.socket.service;

import cn.erika.socket.core.TcpSocket;
import cn.erika.test.socket.handler.AbstractHandler;
import cn.erika.test.socket.handler.DefineString;
import cn.erika.test.socket.handler.Message;
import cn.erika.util.StringUtils;
import cn.erika.util.security.Security;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

public class RequestEncrypt implements SocketService {
    private Logger log = LoggerFactory.getLogger(this.getClass());
    private static final String serviceName = DefineString.REQ_ENCRYPT;

    @Override
    public void service(AbstractHandler handler, TcpSocket socket, Message message) {
//        log.debug("获取服务器公钥: " + Base64.getEncoder().encodeToString(message.getPayload()));
        socket.set(DefineString.PUBLIC_KEY, message.getPayload());
        Security.Type passwordType = Security.Type.AES256ECB;
        String password = StringUtils.randomString(18);
        socket.set(DefineString.PASSWORD, password);
        socket.set(DefineString.PASSWORD_TYPE, passwordType);
        Map<String, Object> body = new HashMap<>();
        body.put("value", Base64.getEncoder().encodeToString(password.getBytes(AbstractHandler.CHARSET)));
        body.put("key", passwordType.getValue());
        handler.sendMessage(socket, new Message(serviceName, body));
        socket.set(DefineString.ENCRYPT, true);
//        log.debug("发送密钥，类型:" + passwordType + ", 密钥:" + password);
    }
}
