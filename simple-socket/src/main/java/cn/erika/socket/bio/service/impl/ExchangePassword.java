package cn.erika.socket.bio.service.impl;

import cn.erika.aop.annotation.Component;
import cn.erika.socket.bio.service.ISocketService;
import cn.erika.socket.common.component.BaseSocket;
import cn.erika.socket.common.component.Message;
import cn.erika.config.Constant;
import cn.erika.config.GlobalSettings;
import cn.erika.util.StringUtils;
import cn.erika.util.security.Security;
import cn.erika.util.security.SecurityException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Base64;
import java.util.HashMap;

@Component(Constant.SRV_EXCHANGE_PASSWORD)
public class ExchangePassword implements ISocketService {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    @Override
    public void client(BaseSocket socket, Message message) {
//        log.debug("获取服务器公钥: " + Base64.getEncoder().encodeToString(message.getPayload()));
        try {
            String serverPublicKey = message.get(Constant.PUBLIC_KEY);
            if (serverPublicKey == null) {
                throw new SecurityException("缺少公钥信息");
            }
            socket.set(Constant.PUBLIC_KEY, Base64.getDecoder().decode(serverPublicKey));

            Security.Type encryptType = GlobalSettings.passwordType;
            String encryptCode = StringUtils.randomString(GlobalSettings.passwordLength);
            socket.set(Constant.ENCRYPT_TYPE, encryptType);
            socket.set(Constant.ENCRYPT_CODE, encryptCode);
            Message request = new Message(Constant.SRV_EXCHANGE_PASSWORD, new HashMap<String, Object>() {
                {
                    put(Constant.ENCRYPT_TYPE, encryptType.getValue());
                    put(Constant.ENCRYPT_CODE, Base64.getEncoder().encodeToString(encryptCode.getBytes(GlobalSettings.charset)));
                }
            });
            socket.send(request);
        } catch (SecurityException e) {
            log.error(e.getMessage(), e);
        }
//        socket.set(Constant.ENCRYPT, true);
//        log.debug("发送密钥，类型:" + passwordType + ", 密钥:" + password);
    }

    @Override
    public void server(BaseSocket socket, Message message) {
        try {
            Security.Type encryptType = Security.Type.getByName(message.get(Constant.ENCRYPT_TYPE));
            String encryptCode = message.get(Constant.ENCRYPT_CODE);
            if (encryptType == null || encryptCode == null) {
                throw new SecurityException("缺少加密信息");
            }
            encryptCode = new String(Base64.getDecoder().decode(encryptCode));
//            log.debug("客户端发送密钥，类型:" + encryptType + ", 密钥:" + encryptCode);

            log.debug("加密协商完成");
            Message reply = new Message(Constant.SRV_EXCHANGE_RESULT, new HashMap<String, Object>() {
                {
                    put(Constant.RESULT, true);
                    put(Constant.MESSAGE, "加密协商成功");
                }
            });
            socket.send(reply);
            socket.set(Constant.ENCRYPT_CODE, encryptCode);
            socket.set(Constant.ENCRYPT_TYPE, encryptType);
            socket.set(Constant.ENCRYPT, true);
        } catch (SecurityException e) {
            log.error("加密协商失败");
            Message reply = new Message(Constant.SRV_EXCHANGE_RESULT, new HashMap<String, Object>() {
                {
                    put(Constant.RESULT, false);
                    put(Constant.MESSAGE, "加密协商失败");
                }
            });
            socket.send(reply);
        }
    }
}
