package cn.erika.socket.service.impl;

import cn.erika.socket.core.TcpSocket;
import cn.erika.socket.handler.AbstractHandler;
import cn.erika.socket.handler.Message;
import cn.erika.socket.handler.StringDefine;
import cn.erika.socket.service.ISocketService;
import cn.erika.util.StringUtils;
import cn.erika.util.security.Security;
import com.alibaba.fastjson.JSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

public class EncryptService implements ISocketService {
    private static final String serviceName = StringDefine.SEVR_EXCHANGE_KEY;
    private Logger log = LoggerFactory.getLogger(this.getClass());

    @Override
    public void client(AbstractHandler handler, TcpSocket socket, Message message) {
//        log.debug("获取服务器公钥: " + Base64.getEncoder().encodeToString(message.getPayload()));
        socket.set(StringDefine.PUBLIC_KEY, message.getPayload());
        Security.Type passwordType = Security.Type.AES256ECB;
        String password = StringUtils.randomString(18);
        socket.set(StringDefine.PASSWORD, password);
        socket.set(StringDefine.PASSWORD_TYPE, passwordType);
        Message response = new Message(serviceName, new HashMap<String, String>() {
            {
                put("key", passwordType.getValue());
                put("value", Base64.getEncoder().encodeToString(password.getBytes(AbstractHandler.CHARSET)));
            }
        });
        handler.sendMessage(socket, response);
//        socket.set(StringDefine.ENCRYPT, true);
//        log.debug("发送密钥，类型:" + passwordType + ", 密钥:" + password);
    }

    @Override
    public void server(AbstractHandler handler, TcpSocket socket, Message message) {
        Map<String, String> body = JSON.parseObject(message.getPayload(), Map.class);
        Security.Type passwordType = Security.Type.getByName(body.get("key"));
        Message reply = null;
        if (passwordType == null) {
            log.error("加密协商失败");
            reply = new Message(StringDefine.SEVR_ENCRYPT_RESULT, new HashMap<String, String>() {
                {
                    put("code", "1");
                    put("message", "加密协商失败");
                }
            });
            handler.sendMessage(socket, reply);
        } else {
            String password = new String(Base64.getDecoder().decode(body.get("value")));

//        log.debug("客户端发送密钥，类型:" + passwordType + ", 密钥:" + password);
            log.debug("加密协商完成");
            reply = new Message(StringDefine.SEVR_ENCRYPT_RESULT, new HashMap<String, String>() {
                {
                    put("code", "0");
                    put("message", "加密协商成功");
                }
            });
            handler.sendMessage(socket, reply);
            socket.set(StringDefine.PASSWORD, password);
            socket.set(StringDefine.PASSWORD_TYPE, passwordType);
            socket.set(StringDefine.ENCRYPT, true);
        }
    }
}
