package cn.erika.test.socket.service.impl;

import cn.erika.socket.core.TcpSocket;
import cn.erika.test.socket.handler.AbstractHandler;
import cn.erika.test.socket.handler.Message;
import cn.erika.test.socket.handler.StringDefine;
import cn.erika.test.socket.service.ISocketService;
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
    public void request(AbstractHandler handler, TcpSocket socket, Message message) {
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
        response.addHead(Message.Head.Type, StringDefine.REQUEST);
        handler.sendMessage(socket, response);
//        socket.set(StringDefine.ENCRYPT, true);
//        log.debug("发送密钥，类型:" + passwordType + ", 密钥:" + password);
    }

    @Override
    public void response(AbstractHandler handler, TcpSocket socket, Message message) {
        Map<String, String> body = JSON.parseObject(message.getPayload(), Map.class);
        Security.Type passwordType = Security.Type.getByName(body.get("key"));
        Message response = null;
        if (passwordType == null) {
            log.error("加密协商失败");
            response = new Message(StringDefine.SEVR_ENCRYPT_RESULT, new HashMap<String, String>() {
                {
                    put("code", "1");
                    put("message", "加密协商失败");
                }
            });
            response.addHead(Message.Head.Type, StringDefine.RESPONSE);
            handler.sendMessage(socket, response);
        } else {
            String password = new String(Base64.getDecoder().decode(body.get("value")));

//        log.debug("客户端发送密钥，类型:" + passwordType + ", 密钥:" + password);
            log.debug("加密协商完成");
            response = new Message(StringDefine.SEVR_ENCRYPT_RESULT, new HashMap<String, String>() {
                {
                    put("code", "0");
                    put("message", "加密协商成功");
                }
            });
            response.addHead(Message.Head.Type, StringDefine.RESPONSE);
            handler.sendMessage(socket, response);
            socket.set(StringDefine.PASSWORD, password);
            socket.set(StringDefine.PASSWORD_TYPE, passwordType);
            socket.set(StringDefine.ENCRYPT, true);
        }
    }
}
