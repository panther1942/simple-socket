package cn.erika.test.socket.service;

import cn.erika.socket.core.TcpSocket;
import cn.erika.test.socket.handler.AbstractHandler;
import cn.erika.test.socket.handler.DefineString;
import cn.erika.test.socket.handler.Message;
import cn.erika.util.security.Security;
import com.alibaba.fastjson.JSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Base64;
import java.util.Map;

public class ResponseEncrypt implements SocketService {
    private Logger log = LoggerFactory.getLogger(this.getClass());
    private static final String serviceName = DefineString.RESP_ENCRYPT;

    @Override
    public void service(AbstractHandler handler, TcpSocket socket, Message message) {
        Map<String, String> body = JSON.parseObject(message.getPayload(), Map.class);
        Security.Type passwordType = Security.Type.getByName(body.get("key"));
        if (passwordType == null) {
            log.error("加密协商失败");
            handler.sendMessage(socket, new Message(DefineString.ENCRYPT_FAILED, "加密协商失败"));
            handler.close(socket);
        } else {
            String password = new String(Base64.getDecoder().decode(body.get("value")));
            socket.set(DefineString.PASSWORD, password);
            socket.set(DefineString.PASSWORD_TYPE, passwordType);
            socket.set(DefineString.ENCRYPT, true);
//        log.debug("客户端发送密钥，类型:" + passwordType + ", 密钥:" + password);
            log.debug("加密协商完成");
            handler.sendMessage(socket, new Message(DefineString.ENCRYPT_SUCCESS, "加密协商成功"));
        }

    }
}
